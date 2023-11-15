package me.mrnavastar.protoweaver.api;

import io.netty.buffer.ByteBuf;

public interface ProtoPacket {

    /**
     * This function is called to encode the packet into a {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to write packet data to.
     */
    void encode(ByteBuf buf);

    /**
     * This function is called to decode the packet from a {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to read packet data from.
     * @throws IndexOutOfBoundsException If a {@link ByteBuf} read call fails.
     */
    void decode(ByteBuf buf) throws IndexOutOfBoundsException;
}