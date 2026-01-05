package me.mrnavastar.protoweaver.loader;

import io.netty.channel.Channel;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.ProtoDeterminer;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import me.mrnavastar.r.R;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class Paper extends JavaPlugin implements ProtoLogger.IProtoLogger {

    private final Logger logger = getLogger();
    private boolean setup = false;

    @Override
    public void onEnable() {
        if (!ProtoWeaver.getLoadedProtocols().isEmpty()) setup();
        else ProtoWeaver.PROTOCOL_LOADED.register(protocol -> setup());
    }

    private void setup() {
        if (setup) return;
        ProtoLogger.setLogger(this);

        try {
            R.of(Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder"))
                .call("addListener",
                        R.TypeBinding.of(Key.class, new NamespacedKey("protoweaver", "internal")),
                        R.of(this).implement("io.papermc.paper.network.ChannelInitializeListener")
                );

            VelocityAuth.setSecret(R.of(R.of(Class.forName("io.papermc.paper.configuration.GlobalConfiguration"))
                    .call("get", Object.class))
                    .of("proxies")
                    .of("velocity")
                    .get("secret", String.class).getBytes(StandardCharsets.UTF_8)
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        SSLContext.init(getDataFolder().getAbsolutePath());
        setup = true;
    }

    @SuppressWarnings("unused")
    public void afterInitChannel(Channel channel) {
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