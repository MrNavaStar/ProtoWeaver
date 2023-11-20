package me.mrnavastar.protoweaver.core.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProtoLogger {

    private static final Logger logger = LogManager.getLogger("ProtoLogger");

    private static void log(Level level, String message) {
        logger.log(level, "[" + ProtoConstants.PROTOWEAVER_NAME + "] " + message);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }
}