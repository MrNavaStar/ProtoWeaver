package me.mrnavastar.protoweaver.api.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.RequiredArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

/**
 * A wrapper that allows for closing the connection after the previously sent {@link ProtoPacket} is done sending.
 */
@RequiredArgsConstructor
public class Sender {

    private final ProtoConnection connection;
    private final ChannelFuture future;

    /**
     * Closes the connection after the previously sent {@link ProtoPacket} is done sending.
     */
    public void disconnect() {
        future.addListener((ChannelFutureListener) channelFuture -> connection.disconnect());
    }
}