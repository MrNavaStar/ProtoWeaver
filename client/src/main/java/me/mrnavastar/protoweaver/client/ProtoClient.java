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
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.client.netty.ProtoTrustManager;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ClientConnectionHandler;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.InternalConnectionHandler;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class ProtoClient {

    @FunctionalInterface
    public interface ConnectionEstablishedHandler {
        void handle(ProtoConnection connection) throws Exception;
    }

    @FunctionalInterface
    public interface ConnectionLostHandler {
        void handle(ProtoConnection connection) throws Exception;
    }

    @Getter
    private final InetSocketAddress address;
    private EventLoopGroup workerGroup = null;
    private ProtoConnection connection = null;
    private final SslContext sslContext;
    private final ArrayList<ConnectionEstablishedHandler> connectionEstablishedHandlers = new ArrayList<>();
    private final ArrayList<ConnectionLostHandler> connectionLostHandlers = new ArrayList<>();

    public ProtoClient(@NonNull InetSocketAddress address, @NonNull String hostsFile) {
        try {
            this.address = address;
            ProtoTrustManager trustManager = new ProtoTrustManager(address.getHostName(), address.getPort(), hostsFile);
            this.sslContext = SslContextBuilder.forClient().trustManager(trustManager.getTm()).build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    public ProtoClient(@NonNull InetSocketAddress address) {
        this(address.getHostName(), address.getPort());
    }

    public ProtoClient(@NonNull String host, int port, @NonNull String hostsFile) {
        this(new InetSocketAddress(host, port), hostsFile);
    }

    public ProtoClient(@NonNull String host, int port) {
        this(host, port, ".");
    }

    public ProtoClient connect(@NonNull Protocol protocol) {
        ProtoWeaver.load(protocol);

        Bootstrap b = new Bootstrap();
        workerGroup = new NioEventLoopGroup();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(@NonNull SocketChannel ch) throws Exception {
                ch.pipeline().addLast("ssl", sslContext.newHandler(ch.alloc(), address.getHostName(), address.getPort()));
                connection = new ProtoConnection(InternalConnectionHandler.getProtocol(), Side.CLIENT, ch);
            }
        });

        ChannelFuture f = b.connect(address);
        new Thread(() -> {
            try {
                f.awaitUninterruptibly();
                if (f.isSuccess()) {
                    ((ClientConnectionHandler) connection.getHandler()).start(connection, protocol.toString());
                    // Wait for protocol to switch to passed in one
                    while (connection == null || connection.isOpen() && !connection.getProtocol().toString().equals(protocol.toString()))
                        Thread.onSpinWait();

                    if (connection.isOpen()) connectionEstablishedHandlers.forEach(handler -> {
                        try {
                            handler.handle(connection);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                f.channel().closeFuture().sync();
                connectionLostHandlers.forEach(handler -> {
                    try {
                        handler.handle(connection);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        }).start();
        return this;
    }

    public boolean isConnected() {
        return !workerGroup.isShutdown() || !workerGroup.isShuttingDown() || connection != null && connection.isOpen();
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
        if (workerGroup != null && !workerGroup.isShutdown()) workerGroup.shutdownGracefully();
    }

    public ProtoClient onConnectionEstablished(@NonNull ConnectionEstablishedHandler handler) {
        connectionEstablishedHandlers.add(handler);
        return this;
    }

    public ProtoClient onConnectionLost(@NonNull ConnectionLostHandler handler) {
        this.connectionLostHandlers.add(handler);
        return this;
    }

    public Sender send(@NonNull Object packet) {
        if (connection != null) return connection.send(packet);
        return Sender.NULL;
    }

    public Protocol getCurrentProtocol() {
        return connection == null ? null : connection.getProtocol();
    }
}