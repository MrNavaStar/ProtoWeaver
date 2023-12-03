package io.papermc.paper.network;

import io.netty.channel.Channel;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface ChannelInitializeListener {
    void afterInitChannel(@NonNull Channel channel);
}