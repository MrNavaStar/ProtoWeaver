package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.api.protocol.protomessage.ProtoMessage;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Fabric extends SSLContext implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        SSLContext.genKeys();
        SSLContext.initContext();

        // Fabric Proxy Lite support
        FabricLoader.getInstance().getModContainer("fabricproxy-lite").ifPresent(modContainer -> {
            // FabricProxyLites config is initialized as a mixin plugin, so it's guaranteed to be loaded before protoweaver
            VelocityAuth.setSecret(one.oktw.FabricProxyLite.config.getSecret());
        });
    }
}