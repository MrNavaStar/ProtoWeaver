package me.mrnavastar.protoweaver.loader;

import io.netty.channel.Channel;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.loader.netty.ProtoDeterminer;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Paper extends JavaPlugin implements ChannelInitializeListener {

    @Override
    public void onEnable() {
        ChannelInitializeListenerHolder.addListener(new NamespacedKey("protoweaver", "internal"), new Paper());
        SSLContext.genKeys();
        SSLContext.initContext();

        VelocityAuth.setSecret(GlobalConfiguration.get().proxies.velocity.secret);
    }

    @Override
    public void afterInitChannel(@NonNull Channel channel) {
        ProtoDeterminer.registerToPipeline(channel);
    }
}