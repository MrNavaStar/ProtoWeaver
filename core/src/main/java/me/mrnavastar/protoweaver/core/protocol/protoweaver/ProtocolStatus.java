package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.util.BufUtils;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolStatus implements ProtoPacket {

    public enum Status {
        MISSING,
        START,
        UPGRADE
    }

    private String currentProtocol;
    private String nextProtocol;
    private Status status;

    @Override
    public void encode(ByteBuf buf) {
        BufUtils.writeString(buf, currentProtocol);
        BufUtils.writeString(buf, nextProtocol);
        buf.writeInt(status.ordinal());
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        currentProtocol = BufUtils.readString(buf);
        nextProtocol = BufUtils.readString(buf);
        status = Status.values()[buf.readInt()];
    }
}