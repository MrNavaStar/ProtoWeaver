package me.mrnavastar.protoweaver.fabric;

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

    public Forge() {
        ProtoLogger.setLogger(this);
        SSLContext.initKeystore(FMLConfig.defaultConfigPath() + "/protoweaver");
        SSLContext.genKeys();
        SSLContext.initContext();
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