package me.mrnavastar.protoweaver.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.util.DrunkenBishop;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoPacketHandler extends ByteToMessageDecoder {

    private static ConcurrentHashMap<String, Integer> connectionCount;

    private final ProtoConnection connection;
    @Setter
    private ProtoConnectionHandler handler;
    private ChannelHandlerContext ctx;
    private ByteBuf buf = Unpooled.buffer();

    public ProtoPacketHandler(ProtoConnection connection, ConcurrentHashMap<String, Integer> connectionCount) {
        this.connection = connection;
        ProtoPacketHandler.connectionCount = connectionCount;
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
            connectionCount.put(connection.getProtocol().toString(), connectionCount.getOrDefault(connection.getProtocol().toString(), 1) - 1);
            this.handler.onDisconnect(connection);
        } catch (Exception e) {
            connection.getProtocol().logErr("Threw an error on disconnect!");
            e.printStackTrace();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        // Ensure the whole packet has arrived before trying to decode
        if (byteBuf.readableBytes() < 4) return;
        byteBuf.markReaderIndex();
        int packetLen = byteBuf.readInt();
        if (byteBuf.readableBytes() < packetLen) {
            byteBuf.resetReaderIndex();
            return;
        }

        Object packet = null;
        try {
            byte[] bytes = new byte[packetLen];
            byteBuf.readBytes(bytes);
            packet = connection.getProtocol().deserialize(bytes);
            handler.handlePacket(connection, packet);

        } catch (IllegalArgumentException e) {
            connection.getProtocol().logWarn("Ignoring an " + e.getMessage());
        } catch (Exception e) {
            if (packet != null) connection.getProtocol().logErr("Threw an error when trying to handle: " + packet.getClass() + "!");
            e.printStackTrace();
        }
    }

    // Done with two bufs to prevent the user from messing with the internal data
    public Sender send(Object packet) {
        try {
            byte[] packetBuf = connection.getProtocol().serialize(packet);
            if (packetBuf.length == 0) return new Sender(connection, ctx.newSucceededFuture(), false);

            buf.writeInt(packetBuf.length); // Packet Len
            buf.writeBytes(packetBuf);

            Sender sender = new Sender(connection, ctx.writeAndFlush(buf), true);
            buf = Unpooled.buffer();
            return sender;

        } catch (IllegalArgumentException e) {
            connection.getProtocol().logErr("Tried to send an " + e.getMessage());
            return new Sender(connection, ctx.newSucceededFuture(), false);
        } catch (Exception e) {
            connection.getProtocol().logErr("Threw an error when trying to send: " + packet.getClass() + "!");
            e.printStackTrace();
            return new Sender(connection, ctx.newSucceededFuture(), false);
        }
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

            ProtoLogger.err("Failed to connect to: " + parts[2] + ":" + parts[3]);
            ProtoLogger.err("Server SSL fingerprint does not match saved fingerprint! This could be a MITM ATTACK!");
            ProtoLogger.err(" - https://en.wikipedia.org/wiki/Man-in-the-middle_attack");
            ProtoLogger.err("If you've reset your server configuration recently, you can probably ignore this and reset/remove the \"protoweaver.hosts\" file.");

            ctx.close();
            connection.disconnect();
        }
    }
}