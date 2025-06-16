package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLLoader;
import org.adde0109.pcf.v1_20_2.neoforge.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;


@Mod(ProtoConstants.PROTOWEAVER_ID)
public class NeoForge implements ProtoLogger.IProtoLogger {

    private final Logger logger = LogManager.getLogger();
    private boolean setup = false;

    public NeoForge() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void serverStarted(FMLDedicatedServerSetupEvent event) {
        if (!ProtoWeaver.getLoadedProtocols().isEmpty()) setup();
        else ProtoWeaver.PROTOCOL_LOADED.register(protocol -> setup());
    }

    private void setup() {
        if (setup) return;
        ProtoLogger.setLogger(this);
        SSLContext.init(FMLConfig.defaultConfigPath() + "/protoweaver");

        // Proxy Compatible Forge support
        if (FMLLoader.getLoadingModList().getModFileById("proxy-compatible-forge") != null) {
            // Proxy Compatible Forge's config becomes available after FMLServerAboutToStartEvent
            VelocityAuth.setSecret(Config.config.forwardingSecret.get().getBytes(StandardCharsets.UTF_8));
        }
        setup = true;
    }

    @Override
    public void info(String message) {
        logger.info("[" + ProtoConstants.PROTOWEAVER_NAME + "]: {}", message);
    }

    @Override
    public void warn(String message) {
        logger.info("[" + ProtoConstants.PROTOWEAVER_NAME + "]: {}", message);
    }

    @Override
    public void err(String message) {
        logger.info("[" + ProtoConstants.PROTOWEAVER_NAME + "]: {}", message);
    }
}