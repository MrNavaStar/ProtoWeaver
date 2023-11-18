package me.mrnavastar.protoweaver.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.Sender;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

public class ProtoPacketSender extends SimpleChannelInboundHandler<ProtoPacket> {

    private final ProtoConnection connection;
    private ChannelHandlerContext ctx;
    private ByteBuf buf = Unpooled.buffer();

    public ProtoPacketSender(ProtoConnection connection) {
        this.connection = connection;
        if (connection.getSide().equals(Side.CLIENT)) {
            buf.writeByte(0); // Fake out minecraft packet len
            buf.writeByte(ProtoConstants.PROTOWEAVER_MAGIC_BYTE);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoPacket msg) {
        send(msg);
    }

    // Done with two bufs to prevent the user from messing with the internal data
    public Sender send(ProtoPacket packet) {
        ByteBuf packetBuf = Unpooled.buffer();
        try {
            packet.encode(packetBuf);
        } catch (Exception e) {
            ProtoLogger.error("Failed to encode packet: " + packet.getClass().getName());
            ProtoLogger.error(e.getMessage());
            return new Sender(ctx.newSucceededFuture());
        }

        buf.writeInt(packetBuf.readableBytes()); // Packet Len
        buf.writeInt(connection.getProtocol().getPacketId(packet)); // Packet Id
        buf.writeBytes(packetBuf); // Combine bufs

        Sender sender = new Sender(ctx.writeAndFlush(buf));
        System.out.println("SENT DATA DAWG!!");
        buf = Unpooled.buffer();
        return sender;
    }
}