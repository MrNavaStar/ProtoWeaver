package me.mrnavastar.protoweaver.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.client.netty.ProtoTrustManager;
import me.mrnavastar.protoweaver.core.netty.ProtoConnection;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ClientConnectionHandler;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.InternalConnectionHandler;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;

public class ProtoWeaverClient {

    @FunctionalInterface
    public interface ConnectionLostHandler {
        void handle(ProtoConnection connection);
    }

    @Getter
    private final InetSocketAddress address;
    private EventLoopGroup workerGroup = null;
    private ProtoConnection connection = null;
    private final SslContext sslContext;
    private ConnectionLostHandler connectionLostHandler;

    public ProtoWeaverClient(InetSocketAddress address, String hostsFile) {
        try {
            this.address = address;
            ProtoTrustManager trustManager = new ProtoTrustManager(address.getHostName(), address.getPort(), hostsFile);
            this.sslContext = SslContextBuilder.forClient().trustManager(trustManager.getTm()).build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
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

    public ChannelFuture connect(Protocol protocol) {
        Bootstrap b = new Bootstrap();
        workerGroup = new NioEventLoopGroup();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(@NonNull SocketChannel ch) throws NoSuchMethodException {
                ch.pipeline().addLast("ssl", sslContext.newHandler(ch.alloc(), address.getHostName(), address.getPort()));
                connection = new ProtoConnection(InternalConnectionHandler.getProtocol(), Side.CLIENT, ch);
            }
        });

        ChannelFuture f = b.connect(address);
        new Thread(() -> {
            try {
                f.awaitUninterruptibly();
                if (!f.isSuccess()) return;

                ((ClientConnectionHandler) connection.getHandler()).start(connection, protocol.getName());

                // Wait for protocol to switch to passed in one
                while (connection == null || connection.isOpen() && !connection.getProtocol().getName().equals(protocol.getName())) {
                    Thread.onSpinWait();
                }

                f.channel().closeFuture().sync();
                connectionLostHandler.handle(connection);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        }).start();
        return f;
    }

    public boolean isConnected() {
        return !workerGroup.isShutdown() || !workerGroup.isShuttingDown() || connection != null && connection.isOpen();
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
        if (workerGroup != null) workerGroup.shutdownGracefully();
    }

    public void onConnectionLost(ConnectionLostHandler handler) {
        this.connectionLostHandler = handler;
    }

    @SneakyThrows
    public void send(ProtoPacket packet) {
        if (connection != null) connection.send(packet);
    }

    public Protocol getCurrentProtocol() {
        return connection == null ? null : connection.getProtocol();
    }
}