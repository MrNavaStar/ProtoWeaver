package me.mrnavastar.protoweaver.api;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public abstract class ProtoPacket {

    protected int id;

    public final void setId(int id) {
        this.id = id;
    }

    public abstract void encode(ByteBuf buf);

    public abstract void decode(ByteBuf buf) throws IndexOutOfBoundsException;
}