package me.mrnavastar.protoweaver.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.Getter;
import lombok.NonNull;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.client.netty.ProtoTrustManager;
import me.mrnavastar.protoweaver.core.netty.ProtoConnection;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ClientConnectionHandler;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.InternalConnectionHandler;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class ProtoWeaverClient {

    @FunctionalInterface
    public interface DisconnectHandler {
        void onDisconnect(ProtoConnection connection);
    }

    @Getter
    private final InetSocketAddress address;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ProtoConnection connection = null;
    private DisconnectHandler disconnectHandler = null;
    private final ProtoTrustManager trustManager;

    public ProtoWeaverClient(InetSocketAddress address, String hostsFile) {
        this.address = address;
        this.trustManager = new ProtoTrustManager(address.getHostName(), address.getPort(), hostsFile);
    }

    public ProtoWeaverClient(InetSocketAddress address) {
        this(address, "./protoweaver_hosts");
    }

    public ProtoWeaverClient(String host, int port, String hostsFile) {
        this(new InetSocketAddress(host, port), hostsFile);
    }

    public ProtoWeaverClient(String host, int port) {
        this(host, port, "./protoweaver_hosts");
    }

    private ChannelFuture doConnect(Bootstrap b, EventLoopGroup workerGroup, Protocol protocol, int tries) throws InterruptedException {
        if (workerGroup.isShutdown() || workerGroup.isShuttingDown()) return null;

        ChannelFuture f = b.connect(address);
        f.awaitUninterruptibly();
        if (f.isSuccess()) {
            ((ClientConnectionHandler) connection.getHandler()).start(connection, protocol.getName());
            return f;
        }

        if (tries == 1) return null;
        Thread.sleep(5000);
        return doConnect(b, workerGroup, protocol, tries - 1);
    }

    public CompletableFuture<Boolean> connect(Protocol protocol, int tries) {
        CompletableFuture<Boolean> connected = new CompletableFuture<>();
        new Thread(() -> {
            try {
                SslContext sslCtx = SslContextBuilder.forClient().trustManager(trustManager.getTm()).build();

                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.option(ChannelOption.TCP_NODELAY, true);
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(@NonNull SocketChannel ch) throws NoSuchMethodException {
                        ch.pipeline().addLast("ssl", sslCtx.newHandler(ch.alloc(), address.getHostName(), address.getPort()));
                        connection = new ProtoConnection(InternalConnectionHandler.getProtocol(), Side.CLIENT, ch);
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        System.out.println(cause.getMessage());
                    }
                });

                ChannelFuture f = doConnect(b, workerGroup, protocol, tries);
                if (f == null) {
                    connected.complete(false);
                    return;
                }

                // Wait for protocol to switch to passed in one
                while (connection == null || connection.isOpen() && !connection.getProtocol().getName().equals(protocol.getName())) {
                    Thread.onSpinWait();
                }

                connected.complete(connection != null && connection.isOpen());
                f.channel().closeFuture().sync();
            } catch (SSLException | InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        }).start();
        return connected;
    }

    public CompletableFuture<Boolean> connect(Protocol protocol) {
        return connect(protocol, 1);
    }

    public CompletableFuture<Boolean> connectForever(Protocol protocol) {
        return connect(protocol, -1);
    }

    public boolean isConnected() {
        return !workerGroup.isShutdown() || !workerGroup.isShuttingDown() || connection != null && connection.isOpen();
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
            disconnectHandler.onDisconnect(connection);
            connection = null;
        }
        workerGroup.shutdownGracefully();
    }

    public void onDisconnect(DisconnectHandler handler) {
        disconnectHandler = handler;
    }

    public void send(ProtoPacket packet) {
        if (connection != null) connection.send(packet);
    }

    public Protocol getCurrentProtocol() {
        return connection == null ? null : connection.getProtocol();
    }
}