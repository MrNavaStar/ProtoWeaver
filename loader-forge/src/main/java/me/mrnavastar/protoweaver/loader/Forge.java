package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(ProtoConstants.PROTOWEAVER_ID)
public class Forge implements ProtoLogger.IProtoLogger {

    private final Logger logger = LogManager.getLogger();
    private boolean setup = false;

    public Forge() {
        ProtoLogger.setLogger(this);

        ProtoWeaver.PROTOCOL_LOADED.register(protocol -> {
            if (setup) return;
            SSLContext.initKeystore(FMLConfig.defaultConfigPath() + "/protoweaver");
            SSLContext.genKeys();
            SSLContext.initContext();
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