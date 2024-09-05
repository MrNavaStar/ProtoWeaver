package me.mrnavastar.protoweaver.loader.netty;

import com.velocitypowered.api.proxy.ProxyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.RequiredArgsConstructor;
import me.mrnavastar.r.R;

@RequiredArgsConstructor
public class WrappedChannelInitializer extends ChannelInitializer<Channel> {

    private final ChannelInitializer<Channel> initializer;

    public static void install(ProxyServer proxy) {
        R holder = R.of(proxy).of("cm").of("serverChannelInitializer");
        holder.set("initializer", new WrappedChannelInitializer(holder.get("initializer", ChannelInitializer.class)));
    }

    @Override
    protected void initChannel(Channel ch) {
        R.of(initializer).call("initChannel", ch);
        ProtoDeterminer.registerToPipeline(ch);
    }
}