package me.mrnavastar.protoweaver.mod;

import me.mrnavastar.protoweaver.mod.netty.SslContext;
import net.fabricmc.api.DedicatedServerModInitializer;

public class Fabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        SslContext.init();
    }
}
