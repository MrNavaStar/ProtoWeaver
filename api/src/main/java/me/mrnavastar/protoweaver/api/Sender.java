package me.mrnavastar.protoweaver.api;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.RequiredArgsConstructor;

/**
 * A wrapper that allows for closing the connection after the previously sent {@link ProtoPacket} is done sending.
 */
@RequiredArgsConstructor
public class Sender {

    private final ChannelFuture future;

    /**
     * Closes the connection after the previously sent {@link ProtoPacket} is done sending.
     */
    public void disconnect() {
        future.addListener(ChannelFutureListener.CLOSE);
    }
}