package me.mrnavastar.protoweaver.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.NonNull;
import me.mrnavastar.protoweaver.util.ProtoConstants;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.protocol.protoweaver.Handshake;

public class ProtoPacketSender extends SimpleChannelInboundHandler<ProtoPacket> {

    private final ProtoConnection connection;
    private ChannelHandlerContext ctx;

    public ProtoPacketSender(ProtoConnection connection) {
        this.connection = connection;
    }

    @Override
    public void channelActive(@NonNull ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoPacket msg) {
        send(msg);
    }

    // Done with two bufs to prevent the user from messing with the internal data
    public void send(ProtoPacket packet) {
        ByteBuf buf = Unpooled.buffer();
        ByteBuf packetBuf = Unpooled.buffer();
        packet.encode(packetBuf);

        if (packet instanceof Handshake) {
            buf.writeByte(0);
            buf.writeByte(ProtoConstants.PROTOWEAVER_MAGIC_BYTE);
        }

        buf.writeInt(packetBuf.readableBytes()); // Packet Len
        buf.writeInt(connection.getProtocol().getPacketId(packet)); // Packet Id
        buf.writeBytes(packetBuf); // Combine bufs
        ctx.writeAndFlush(buf);
    }
}