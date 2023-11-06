package me.mrnavastar.protoweaver.core.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.core.util.DrunkenBishop;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.security.cert.CertificateException;
import java.util.Arrays;
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
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() == 0) return;

        int packetLen = byteBuf.readInt();
        int packetID = byteBuf.readInt();
        ProtoPacket packet = connection.getProtocol().getPacket(packetID);
        if (packet == null) {
            unknownPacket(connection.getProtocol(), packetID);
            return;
        }

        try {
            packet.decode(byteBuf.readBytes(packetLen));
        } catch (IndexOutOfBoundsException e) {
            failedToDecodePacket(connection.getProtocol(), packetID, packet.getClass());
        }

        handler.handlePacket(connection, packet);
    }

    private void unknownPacket(Protocol protocol, int packetID) {

    }

    private void failedToDecodePacket(Protocol protocol, int packetID, Class<? extends ProtoPacket> packetClass) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String message = cause.getMessage();
        String[] parts = message.split(":");

        if (parts[1].contains("protoweaver-client-cert-error")) {
            String[] fingerprints = parts[4].split("!=");

            System.out.println(" Saved Fingerprint:     Server Fingerprint:");
            System.out.println(DrunkenBishop.inlineImages(DrunkenBishop.parse(fingerprints[0]), DrunkenBishop.parse(fingerprints[1])));
            System.err.println(
                "Failed to connect to: " + parts[2] + ":" + parts[3] +
                "\nServer SSL fingerprint does not match saved fingerprint! This could be a MITM ATTACK!\n" +
                " - https://en.wikipedia.org/wiki/Man-in-the-middle_attack\n" +
                "If you've reset your server configuration recently, you can probably ignore this and reset/remove the \"protoweaver_hosts\" file.\n");
            connection.disconnect();
        }
    }
}