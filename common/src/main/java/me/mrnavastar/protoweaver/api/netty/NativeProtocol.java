package me.mrnavastar.protoweaver.api.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface NativeProtocol {

    boolean claim(int magic1, int magic2);

    default boolean supportsSSL() {
        return true;
    }

    default boolean resetPipe() {
        return true;
    };

    default void start(ChannelHandlerContext ctx, ByteBuf buf, boolean isSSL) {}
}