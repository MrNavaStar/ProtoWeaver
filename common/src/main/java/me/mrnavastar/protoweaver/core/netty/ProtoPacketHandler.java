package me.mrnavastar.protoweaver.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.util.DrunkenBishop;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.util.List;

public class ProtoPacketHandler extends ByteToMessageDecoder {

    private final ProtoConnection connection;
    @Setter
    private ProtoConnectionHandler handler;
    private ChannelHandlerContext ctx;
    private ByteBuf buf = Unpooled.buffer();

    public ProtoPacketHandler(ProtoConnection connection) {
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
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            this.handler.onDisconnect(connection);
        } catch (Exception e) {
            ProtoLogger.error("Protocol: " + connection.getProtocol().getName() + " threw an error on disconnect!");
            e.printStackTrace();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() == 0) return;

        byte[] bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);
        Object packet = connection.getProtocol().deserialize(bytes);
        if (packet == null) {
            ProtoLogger.error("Protocol: " + connection.getProtocol().getName() + " received an unknown object!");
            return;
        }

        try {
            handler.handlePacket(connection, packet);
        } catch (Exception e) {
            ProtoLogger.error("Protocol: " + connection.getProtocol().getName() + " threw an error on when trying to handle: " + packet.getClass() + "!");
            e.printStackTrace();
        }
    }

    // Done with two bufs to prevent the user from messing with the internal data
    public Sender send(Object packet) {
        try {
            byte[] packetBuf = connection.getProtocol().serialize(packet);
            buf.writeInt(packetBuf.length); // Packet Len
            buf.writeBytes(packetBuf);
        } catch (Exception e) {
            ProtoLogger.error("Failed to encode object: " + packet.getClass().getName());
            e.printStackTrace();
            return new Sender(connection, ctx.newSucceededFuture());
        }

        Sender sender = new Sender(connection, ctx.writeAndFlush(buf));
        buf = Unpooled.buffer();
        return sender;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String message = cause.getMessage();
        String[] parts = message.split(":");

        if (parts[1].contains("protoweaver-client-cert-error")) {
            String[] fingerprints = parts[4].split("!=");

            ProtoLogger.warn(" Saved Fingerprint:     Server Fingerprint:");
            String images = DrunkenBishop.inlineImages(DrunkenBishop.parse(fingerprints[0]), DrunkenBishop.parse(fingerprints[1]));
            for (String line : images.split("\n")) {
                ProtoLogger.warn(line);
            }

            ProtoLogger.error("Failed to connect to: " + parts[2] + ":" + parts[3]);
            ProtoLogger.error("Server SSL fingerprint does not match saved fingerprint! This could be a MITM ATTACK!");
            ProtoLogger.error(" - https://en.wikipedia.org/wiki/Man-in-the-middle_attack");
            ProtoLogger.error("If you've reset your server configuration recently, you can probably ignore this and reset/remove the \"protoweaver_hosts\" file.");

            connection.disconnect();
        }
    }
}