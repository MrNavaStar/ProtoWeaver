package me.mrnavastar.protoweaver.mod;

import net.fabricmc.api.DedicatedServerModInitializer;

public class Fabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ProtoServer.init();
    }
}
