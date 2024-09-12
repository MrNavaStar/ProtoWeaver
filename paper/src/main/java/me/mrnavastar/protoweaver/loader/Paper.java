package me.mrnavastar.protoweaver.loader;

import io.netty.channel.Channel;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.network.ChannelInitializeListener;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.protomessage.ProtoMessage;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.ProtoDeterminer;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class Paper extends JavaPlugin implements ChannelInitializeListener, ProtoLogger.IProtoLogger {

    private final Logger logger = getLogger();
    private boolean setup = false;

    @Override
    public void onEnable() {
        if (!ProtoWeaver.getLoadedProtocols().isEmpty()) setup();
        else ProtoWeaver.PROTOCOL_LOADED.register(protocol -> setup());

        ProtoWeaver.load(ProtoMessage.getProtocol());
    }

    private void setup() {
        if (setup) return;
        ProtoLogger.setLogger(this);
        ChannelInitializeListenerHolder.addListener(new NamespacedKey("protoweaver", "internal"), this);
        SSLContext.initKeystore(getDataFolder().getAbsolutePath());
        SSLContext.genKeys();
        SSLContext.initContext();

        VelocityAuth.setSecret(GlobalConfiguration.get().proxies.velocity.secret.getBytes(StandardCharsets.UTF_8));
        setup = true;
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
    public void err(String message) {
        logger.severe(message);
    }
}