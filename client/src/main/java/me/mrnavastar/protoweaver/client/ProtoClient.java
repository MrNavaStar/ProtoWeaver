package me.mrnavastar.protoweaver.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.NonNull;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;
import me.mrnavastar.protoweaver.protocol.internal.Internal;
import me.mrnavastar.protoweaver.protocol.internal.Handshake;
import me.mrnavastar.protoweaver.protocol.protomessage.Message;

public class ProtoClient {

    private final Protocol protocol;
    private final String host;
    private final int port;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ProtoConnection connection;

    public ProtoClient(Protocol protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public void connect() {
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(@NonNull SocketChannel ch) {
                    Protocol internal = Internal.getProtocol();
                    connection = new ProtoConnection(internal, internal.getNewServerHandler(), ch.pipeline());
                }
            });

            ChannelFuture f = b.connect(host, port).sync();

            connection.send(new Handshake(protocol.getName()));
            connection.upgradeProtocol(protocol, protocol.getNewClientHandler());

            //f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            disconnect();
        }

        System.out.println("Connecting");
    }

    public void disconnect() {
        connection.disconnect();
        workerGroup.shutdownGracefully();
    }

    public void send(ProtoPacket packet) {
        connection.send(packet);
    }
}