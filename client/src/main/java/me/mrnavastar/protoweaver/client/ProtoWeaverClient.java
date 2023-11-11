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
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.client.netty.ProtoTrustManager;
import me.mrnavastar.protoweaver.client.protocol.protoweaver.ClientHandler;
import me.mrnavastar.protoweaver.core.netty.ProtoConnection;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ProtoWeaver;

import javax.net.ssl.SSLException;

public class ProtoWeaverClient {

    @Getter
    private final Protocol protocol;
    @Getter
    private final String host;
    @Getter
    private final int port;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ProtoConnection connection;
    private final ProtoTrustManager trustManager;
    private Thread thread;

    public ProtoWeaverClient(Protocol protocol, String host, int port, String hostsFile) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.trustManager = new ProtoTrustManager(host, port, hostsFile);
        ProtoWeaver.load(protocol);
    }

    public ProtoWeaverClient(Protocol protocol, String host, int port) {
        this(protocol, host, port, "./protoweaver_hosts");
    }

    public void connect() {
        thread = new Thread(() -> {
            try {
                SslContext sslCtx = SslContextBuilder.forClient().trustManager(trustManager.getTm()).build();

                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.option(ChannelOption.TCP_NODELAY, true);
                b.handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(@NonNull SocketChannel ch) {
                        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        connection = new ProtoConnection(ClientHandler.getClientProtocol(), protocol, Side.CLIENT, ch);
                    }
                });

                ChannelFuture f = b.connect(host, port).sync();
                connection.getHandler().onReady(connection);
                f.channel().closeFuture().sync();
            } catch (InterruptedException | SSLException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        });

        thread.start();
        // Block until connection is set up with the specified protocol
        while (connection == null || connection.isOpen() && !connection.getProtocol().getName().equals(protocol.getName())) Thread.onSpinWait();
    }

    public void disconnect() {
        if (connection != null) connection.disconnect();
        workerGroup.shutdownGracefully();
        if (thread != null) thread.interrupt();
    }

    public void send(ProtoPacket packet) {
        if (connection != null) connection.send(packet);
    }
}