package me.mrnavastar.protoweaver.loader.external;

// Wrapper for FabricProxyLite config
public class FabricProxyLite {

    private static String loadedSecret = null;

    public static void init() {
        // FabricProxyLites config is initialized as a mixin plugin, so it's guaranteed to be loaded before protoweaver
        //loadedSecret = one.oktw.FabricProxyLite.config.getSecret();
    }

    public static boolean validate(String secret) {
        return loadedSecret == null || loadedSecret.equals(secret);
    }
}