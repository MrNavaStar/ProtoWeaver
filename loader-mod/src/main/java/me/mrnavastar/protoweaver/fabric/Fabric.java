package me.mrnavastar.protoweaver.fabric;

import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Fabric implements DedicatedServerModInitializer, ProtoLogger.IProtoLogger {

    private final Logger logger = LogManager.getLogger();

    @Override
    public void onInitializeServer() {
        ProtoLogger.setLogger(this);
        SSLContext.genKeys();
        SSLContext.init(FabricLoader.getInstance().getConfigDir() + "/protoweaver");

        // Fabric Proxy Lite support
        FabricLoader.getInstance().getModContainer("fabricproxy-lite").ifPresent(modContainer -> {
            // FabricProxyLites config is initialized as a mixin plugin, so it's guaranteed to be loaded before protoweaver
            VelocityAuth.setSecret(one.oktw.FabricProxyLite.config.getSecret());
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