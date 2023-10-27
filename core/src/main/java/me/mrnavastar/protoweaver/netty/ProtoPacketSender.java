package me.mrnavastar.protoweaver.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.protocol.Side;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtocolStatus;
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
        this.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtoPacket msg) {
        send(msg);
    }

    @RequiredArgsConstructor
    public static class Sender {

        private final ChannelFuture future;

        public void disconnect() {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    // Done with two bufs to prevent the user from messing with the internal data
    public Sender send(ProtoPacket packet) {
        ByteBuf buf = Unpooled.buffer();
        ByteBuf packetBuf = Unpooled.buffer();
        packet.encode(packetBuf);

        // Add ProtoWeaver magic bytes if client sends to server
        if (side.equals(Side.CLIENT) && packet instanceof ProtocolStatus status && status.getStatus().equals(ProtocolStatus.Status.START)) {
            buf.writeByte(0); // Fake out minecraft packet len
            buf.writeByte(ProtoConstants.PROTOWEAVER_MAGIC_BYTE);
        }

        buf.writeInt(packetBuf.readableBytes()); // Packet Len
        buf.writeInt(connection.getProtocol().getPacketId(packet)); // Packet Id
        buf.writeBytes(packetBuf); // Combine bufs
        return new Sender(ctx.writeAndFlush(buf));
    }
}