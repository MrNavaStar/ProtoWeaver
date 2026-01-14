package me.mrnavastar.protoweaver.server.netty;

import me.mrnavastar.protoweaver.api.ProtoWeaver;

public class ProtoInitializer {

    private static boolean setupInvoked = false;

    public static void initialize(String keyStorageDirectory, Runnable setup) {
        SSLContext.init(keyStorageDirectory);

        if (!ProtoWeaver.getLoadedProtocols().isEmpty()) setup(setup);
        else ProtoWeaver.PROTOCOL_LOADED.register(protocol -> setup(setup));
    }

    private static void setup(Runnable setup) {
        if (setupInvoked) return;
        setupInvoked = true;
        setup.run();
    }
}
