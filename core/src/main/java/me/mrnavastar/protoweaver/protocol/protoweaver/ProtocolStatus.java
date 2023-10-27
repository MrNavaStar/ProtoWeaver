package me.mrnavastar.protoweaver.protocol.protoweaver;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.util.BufUtils;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolStatus implements ProtoPacket {

    public enum Status {
        MISSING,
        START,
        UPGRADE
    }

    private String protocol;
    private Status status;

    @Override
    public void encode(ByteBuf buf) {
        BufUtils.writeString(buf, protocol);
        buf.writeInt(status.ordinal());
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        protocol = BufUtils.readString(buf);
        status = Status.values()[buf.readInt()];
    }
}
