package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoPacket;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthStatus implements ProtoPacket {

    public enum Status {
        OK,
        REQUIRED,
        DENIED
    }

    private Status status;

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(status.ordinal());
    }

    @Override
    public void decode(ByteBuf buf) {
        status = Status.values()[buf.readInt()];
    }
}