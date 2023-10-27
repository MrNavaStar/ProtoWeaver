package me.mrnavastar.protoweaver.protocol.protoweaver;

import io.netty.buffer.ByteBuf;
import me.mrnavastar.protoweaver.api.ProtoPacket;

public class ServerAuthState implements ProtoPacket {

    public enum Status {
        OK,
        REQUIRED,
        DENIED
    }

    private Status status;

    public ServerAuthState() {}

    public ServerAuthState(Status status) {
        this.status = status;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(status.ordinal());
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        status = Status.values()[buf.readInt()];
    }
}
