package me.mrnavastar.protoweaver.api;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public abstract class ProtoPacket {

    public abstract void encode(ByteBuf buf);

    public abstract void decode(ByteBuf buf) throws IndexOutOfBoundsException;
}