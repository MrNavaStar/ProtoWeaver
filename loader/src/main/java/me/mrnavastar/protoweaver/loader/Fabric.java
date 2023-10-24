package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import net.fabricmc.api.DedicatedServerModInitializer;

public class Fabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        SSLContext.init();
    }
}
