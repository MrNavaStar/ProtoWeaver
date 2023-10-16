package me.mrnavastar.protoweaver.protocol.internal;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoPacket;

import java.nio.charset.StandardCharsets;

@Getter
public class Handshake extends ProtoPacket {

    private String protocolName;

    public Handshake(String protocolName) {
        this.protocolName = protocolName;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBytes(protocolName.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        int len = buf.readableBytes();
        protocolName = new String(buf.readBytes(len).array(), StandardCharsets.UTF_8);
    }
}