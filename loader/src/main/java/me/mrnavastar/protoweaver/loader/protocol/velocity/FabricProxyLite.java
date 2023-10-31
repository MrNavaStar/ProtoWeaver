package me.mrnavastar.protoweaver.loader.protocol.velocity;

import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;

public class FabricProxyLite {

    public static void init() {
        // FabricProxyLites config is initialized as a mixin plugin, so it's guaranteed to be loaded before protoweaver
        VelocityAuth.setSecret(one.oktw.FabricProxyLite.config.getSecret());
    }
}