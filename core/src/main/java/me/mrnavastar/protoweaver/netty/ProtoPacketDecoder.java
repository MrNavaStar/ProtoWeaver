package me.mrnavastar.protoweaver.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.mrnavastar.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.protocol.internal.Handshake;
import me.mrnavastar.protoweaver.protocol.protomessage.Message;

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
            ProtoWeaver.gotUnknownPacket(connection.getProtocol(), packetID);
            return;
        }

        try {
            packet.decode(byteBuf.readBytes(packetLen));
        } catch (IndexOutOfBoundsException e) {
            ProtoWeaver.failedToDecodePacket(connection.getProtocol(), packetID);
        }

        handler.handlePacket(connection, packet);
    }
}