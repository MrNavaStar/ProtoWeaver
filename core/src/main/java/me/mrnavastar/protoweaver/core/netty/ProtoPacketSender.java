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
        packet.encode(packetBuf);

        // Protocol status gets an id of -1 so that it can be used while any protocol is loaded.
        int id;
        if (packet instanceof ProtocolStatus) id = -1;
        else id = connection.getProtocol().getPacketId(packet);

        buf.writeInt(packetBuf.readableBytes()); // Packet Len
        buf.writeInt(id); // Packet Id
        buf.writeBytes(packetBuf); // Combine bufs

        Sender sender = new Sender(ctx.writeAndFlush(buf));
        buf = Unpooled.buffer();
        return sender;
    }
}