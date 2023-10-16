package me.mrnavastar.protoweaver.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.NonNull;
import me.mrnavastar.protoweaver.ProtoConstants;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.protocol.internal.Handshake;

public class ProtoPacketSender extends SimpleChannelInboundHandler<ProtoPacket> {

    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(@NonNull ChannelHandlerContext ctx) {
        this.ctx = ctx;
        System.out.println("Hellloooo????");
        send(new Handshake("protoweaver:proto-message"));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoPacket msg) {
        send(msg);
    }

    public void send(ProtoPacket packet) {
        ByteBuf buf = Unpooled.buffer();

        if (packet instanceof Handshake) {
            buf.writeByte(0);
            buf.writeByte(ProtoConstants.PROTOWEAVER_MAGIC_BYTE);
        }
        buf.writeInt(packet.getId());

        packet.encode(buf);
        //buf.resetWriterIndex();

        ctx.writeAndFlush(buf);
    }
}