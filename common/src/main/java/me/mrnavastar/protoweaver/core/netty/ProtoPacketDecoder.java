package me.mrnavastar.protoweaver.core.netty;

import com.esotericsoftware.kryo.kryo5.Kryo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.core.util.DrunkenBishop;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.io.IOException;
import java.util.List;

public class ProtoPacketDecoder extends ByteToMessageDecoder {

    private final ProtoConnection connection;
    @Setter
    private ProtoConnectionHandler handler;

    public ProtoPacketDecoder(ProtoConnection connection) {
        this.connection = connection;
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
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws IOException {
        if (byteBuf.readableBytes() == 0) return;

        int packetLen = byteBuf.readInt();
        ProtoPacket packet = connection.getProtocol().deserialize(byteBuf.readBytes(packetLen));
        if (packet == null) {
            ProtoLogger.error("Protocol: " + connection.getProtocol().getName() + " received an unknown packet!");
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