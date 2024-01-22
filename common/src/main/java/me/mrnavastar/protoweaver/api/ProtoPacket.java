package me.mrnavastar.protoweaver.api;

import io.netty.buffer.ByteBuf;

public interface ProtoPacket {

    /**
     * This function is called to encode the packet into a {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to write packet data to.
     */
    void encode(ByteBuf buf) throws Exception;

    /**
     * This function is called to decode the packet from a {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to read packet data from.
     */
    void decode(ByteBuf buf) throws Exception;
}