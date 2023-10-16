package me.mrnavastar.protoweaver.api;

import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;
import me.mrnavastar.protoweaver.netty.ProtoConnection;

public abstract class ProtoPacketHandler {
    @Setter
    protected ChannelHandlerContext ctx;

    public abstract void handlePacket(ProtoConnection connection, ProtoPacket packet);
}