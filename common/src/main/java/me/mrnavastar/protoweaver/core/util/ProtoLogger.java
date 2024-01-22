package me.mrnavastar.protoweaver.core.util;


import lombok.Setter;

public class ProtoLogger {

    public interface IProtoLogger {
        void info(String message);
        void warn(String message);
        void error(String message);
    }

    @Setter
    private static IProtoLogger logger;

    public static void info(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
       logger.warn(message);
    }

    public static void error(String message) {
        logger.error(message);
    }
}