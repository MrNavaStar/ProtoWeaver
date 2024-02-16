package me.mrnavastar.protoweaver.loader;

import io.netty.channel.Channel;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.ProtoDeterminer;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.logging.Logger;

public class Paper extends JavaPlugin implements ChannelInitializeListener, ProtoLogger.IProtoLogger {

    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        ProtoLogger.setLogger(this);
        ChannelInitializeListenerHolder.addListener(new NamespacedKey("protoweaver", "internal"), this);
        SSLContext.initKeystore(getDataFolder().getAbsolutePath());
        SSLContext.genKeys();
        SSLContext.initContext();

        VelocityAuth.setSecret(GlobalConfiguration.get().proxies.velocity.secret);
    }

    @Override
    public void afterInitChannel(@NonNull Channel channel) {
        ProtoDeterminer.registerToPipeline(channel);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
    }
}