package me.mrnavastar.protoweaver.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;
import me.mrnavastar.protoweaver.protocol.internal.Handshake;

public class ClientConnection extends ProtoConnection {

    public ClientConnection(Protocol protocol, ProtoPacketHandler handler, ChannelPipeline pipeline) {
        super(protocol, handler, pipeline);

    }
}
