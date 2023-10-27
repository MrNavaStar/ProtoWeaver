package me.mrnavastar.protoweaver.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.protocol.Side;
import me.mrnavastar.protoweaver.util.ProtoConstants;

public class ProtoPacketSender extends SimpleChannelInboundHandler<ProtoPacket> {

    private final ProtoConnection connection;
    private final Side side;
    private ChannelHandlerContext ctx;

    public ProtoPacketSender(ProtoConnection connection, Side side) {
        this.connection = connection;
        this.side = side;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        if (side.equals(Side.CLIENT)) sendMagicBytes(ctx);
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

        // Add ProtoWeaver identifiers if client sends to server
        /*if (packet instanceof ProtocolStatus status && handshake.from(Handshake.Side.CLIENT)) {
            buf.writeByte(0);
            buf.writeByte(ProtoConstants.PROTOWEAVER_MAGIC_BYTE);
        }*/

        buf.writeInt(packetBuf.readableBytes()); // Packet Len
        buf.writeInt(connection.getProtocol().getPacketId(packet)); // Packet Id
        buf.writeBytes(packetBuf); // Combine bufs
        ctx.writeAndFlush(buf);
    }

    public void sendMagicBytes(ChannelHandlerContext ctx) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0);
        buf.writeByte(ProtoConstants.PROTOWEAVER_MAGIC_BYTE);
        ctx.writeAndFlush(buf);
    }
}