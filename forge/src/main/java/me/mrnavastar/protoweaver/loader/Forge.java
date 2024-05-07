package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLLoader;
import org.adde0109.pcf.Initializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(ProtoConstants.PROTOWEAVER_ID)
public class Forge implements ProtoLogger.IProtoLogger {

    private final Logger logger = LogManager.getLogger();
    private boolean setup = false;

    public Forge() {
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void serverStarted(FMLDedicatedServerSetupEvent event) {
        if (!ProtoWeaver.getLoadedProtocols().isEmpty()) setup();
        else ProtoWeaver.PROTOCOL_LOADED.register(protocol -> setup());
    }

    private void setup() {
        if (setup) return;
        ProtoLogger.setLogger(this);
        SSLContext.initKeystore(FMLConfig.defaultConfigPath() + "/protoweaver");
        SSLContext.genKeys();
        SSLContext.initContext();

        // Proxy Compatible Forge support
        if (FMLLoader.getLoadingModList().getModFileById("proxy-compatible-forge") != null) {
            // Proxy Compatible Forge's config becomes available after FMLServerAboutToStartEvent
            VelocityAuth.setSecret(Initializer.config.forwardingSecret.get());
        }
        setup = true;
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