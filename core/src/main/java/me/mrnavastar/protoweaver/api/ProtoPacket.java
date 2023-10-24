package me.mrnavastar.protoweaver.api;

import io.netty.buffer.ByteBuf;

public interface ProtoPacket {

    void encode(ByteBuf buf);

    void decode(ByteBuf buf) throws IndexOutOfBoundsException;
}