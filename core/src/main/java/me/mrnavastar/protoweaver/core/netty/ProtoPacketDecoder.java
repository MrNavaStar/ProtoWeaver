package me.mrnavastar.protoweaver.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ProtocolStatus;
import me.mrnavastar.protoweaver.core.util.DrunkenBishop;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.util.List;

public class ProtoPacketDecoder extends ByteToMessageDecoder {

    private final ProtoConnection connection;
    private ProtoPacketHandler handler;

    public ProtoPacketDecoder(ProtoConnection connection) {
        this.connection = connection;
    }

    public void setHandler(ProtoPacketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        handler.onDisconnect(connection);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() == 0) return;

        int packetLen = byteBuf.readInt();
        int packetID = byteBuf.readInt();

        ProtoPacket packet;
        if (packetID == -1) {
            packet = new ProtocolStatus();
            handler = ProtoWeaver.getProtocol().newHandler(connection.getSide());
        }
        else packet = connection.getProtocol().getPacket(packetID);
        if (packet == null) {
            ProtoLogger.error("Got unknown packet with id: " + packetID + " on protocol: " + connection.getProtocol().getName());
            return;
        }

        try {
            packet.decode(byteBuf.readBytes(packetLen));
        } catch (IndexOutOfBoundsException e) {
            ProtoLogger.error("Failed to decode packet: " + packet.getClass());
            return;
        }

        handler.handlePacket(connection, packet);
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