package me.mrnavastar.protoweaver.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.NonNull;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.protocol.protoweaver.Handshake;

public class ProtoWeaverClient {

    @Getter
    private Protocol protocol;
    @Getter
    private final String host;
    @Getter
    private final int port;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ProtoConnection connection;
    private Thread thread;

    public ProtoWeaverClient(Protocol protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        ProtoWeaver.load(protocol);
    }

    public void connect() {
        thread = new Thread(() -> {
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(@NonNull SocketChannel ch) {
                        Protocol internal = ProtoWeaver.getProtocol();
                        connection = new ProtoConnection(internal, internal.newClientHandler(), ch.pipeline());
                    }
                });

                ChannelFuture f = b.connect(host, port).sync();
                connection.send(new Handshake(protocol.getName(), Handshake.Side.CLIENT));
                f.channel().closeFuture().sync();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        });

        thread.start();
        // Block until connection is set up with the specified protocol
        while (connection == null || !connection.getProtocol().getName().equals(protocol.getName())) Thread.onSpinWait();
    }

    public void disconnect() {
        if (connection != null) connection.disconnect();
        workerGroup.shutdownGracefully();
        if (thread != null) thread.interrupt();
    }

    public void send(ProtoPacket packet) {
        if (connection != null) connection.send(packet);
    }

    public void upgradeProtocol(Protocol protocol) {
        this.protocol = protocol;
        ProtoWeaver.load(protocol);
        if (connection != null) connection.upgradeProtocol(protocol, protocol.newClientHandler());
    }
}