package me.mrnavastar.protoweaver.loader.natives;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;

public class ProtoJetty implements NativeHttp.Server {

    private final LocalConnector connector;

    public static Server create(String endpoint, LocalConnector connector) {
        ProtoJetty jetty = new ProtoJetty(connector);
        NativeHttp.registerServer(endpoint, jetty);

        Server server = new Server();
        server.addConnector(jetty.connector);
        return server;
    }

    // TODO: This method is kinda sus and may not work
    public static Server create(String endpoint) {
        Server server = new Server();
        ProtoJetty jetty = new ProtoJetty(new LocalConnector(server));
        NativeHttp.registerServer(endpoint, jetty);
        server.addConnector(jetty.connector);
        return server;
    }

    private ProtoJetty(LocalConnector connector) {
        this.connector = connector;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        ctx.writeAndFlush(Unpooled.wrappedBuffer(connector.getResponse(buf.nioBuffer())))
                .addListener(ChannelFutureListener.CLOSE)
                .addListener((future) -> {
                    Throwable cause = future.cause();

                });
    }
}