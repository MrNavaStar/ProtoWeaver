package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import one.oktw.FabricProxyLite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Fabric implements DedicatedServerModInitializer, ProtoLogger.IProtoLogger {

    private final Logger logger = LogManager.getLogger();
    private boolean setup = false;

    @Override
    public void onInitializeServer() {
        ProtoLogger.setLogger(this);

        ProtoWeaver.PROTOCOL_LOADED.register(protocol -> {
            if (setup) return;
            SSLContext.initKeystore(FabricLoader.getInstance().getConfigDir() + "/protoweaver");
            SSLContext.genKeys();
            SSLContext.initContext();

            // Fabric Proxy Lite support
            FabricLoader.getInstance().getModContainer("fabricproxy-lite").ifPresent(modContainer -> {
                // FabricProxyLites config is initialized as a mixin plugin, so it's guaranteed to be loaded before protoweaver
                VelocityAuth.setSecret(FabricProxyLite.config.getSecret());
            });
            setup = true;
        });
    }

    @Override
    public void info(String message) {
        logger.info("[" + ProtoConstants.PROTOWEAVER_NAME + "] " + message);
    }

    @Override
    public void warn(String message) {
        logger.info("[" + ProtoConstants.PROTOWEAVER_NAME + "] " + message);
    }

    @Override
    public void error(String message) {
        logger.info("[" + ProtoConstants.PROTOWEAVER_NAME + "] " + message);
    }
}