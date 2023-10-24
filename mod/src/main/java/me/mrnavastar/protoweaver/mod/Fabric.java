package me.mrnavastar.protoweaver.mod;

import me.mrnavastar.protoweaver.mod.netty.SSLContext;
import net.fabricmc.api.DedicatedServerModInitializer;

public class Fabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        SSLContext.init();
    }
}
