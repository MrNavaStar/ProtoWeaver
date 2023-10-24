package me.mrnavastar.protoweaver.protocol.protoweaver;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoPacket;

import java.nio.charset.StandardCharsets;

public class Handshake implements ProtoPacket {

    public enum Side {
        CLIENT,
        SERVER
    }

    @Getter
    private String protocolName;
    private Side side;

    public Handshake() {}

    public Handshake(String protocolName, Side side) {
        this.protocolName = protocolName;
        this.side = side;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(protocolName.length());
        buf.writeBytes(protocolName.getBytes(StandardCharsets.UTF_8));
        buf.writeInt(side.ordinal());
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        int len = buf.readInt();
        protocolName = buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
        side = Side.values()[buf.readInt()];
    }

    public boolean from(Side side) {
        return this.side.equals(side);
    }
}