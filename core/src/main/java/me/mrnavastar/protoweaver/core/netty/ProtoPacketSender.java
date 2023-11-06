package me.mrnavastar.protoweaver.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.Sender;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ProtocolStatus;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;

public class ProtoPacketSender extends SimpleChannelInboundHandler<ProtoPacket> {

    private final ProtoConnection connection;
    private ChannelHandlerContext ctx;

    public ProtoPacketSender(ProtoConnection connection) {
        this.connection = connection;
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
        ByteBuf buf = Unpooled.buffer();
        ByteBuf packetBuf = Unpooled.buffer();
        packet.encode(packetBuf);

        // Add ProtoWeaver magic bytes if client sends to server
        if (connection.getSide().equals(Side.CLIENT) && packet instanceof ProtocolStatus status && status.getStatus().equals(ProtocolStatus.Status.START)) {
            buf.writeByte(0); // Fake out minecraft packet len
            buf.writeByte(ProtoConstants.PROTOWEAVER_MAGIC_BYTE);
        }

        buf.writeInt(packetBuf.readableBytes()); // Packet Len
        buf.writeInt(connection.getProtocol().getPacketId(packet)); // Packet Id
        buf.writeBytes(packetBuf); // Combine bufs
        return new Sender(ctx.writeAndFlush(buf));
    }
}