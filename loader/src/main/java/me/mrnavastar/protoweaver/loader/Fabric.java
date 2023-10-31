package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.loader.protocol.velocity.FabricProxyLite;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import me.mrnavastar.protoweaver.api.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.api.protocol.protomessage.ProtoMessage;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.core.util.DrunkenBishop;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Fabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        SSLContext.genKeys();
        SSLContext.init();
        FabricLoader.getInstance().getModContainer("fabricproxy-lite").ifPresent(modContainer -> FabricProxyLite.init());

        // Testing
        ProtoWeaver.load(ProtoMessage.getProtocol());
        ProtoMessage.MESSAGE_RECEIVED.register((connection, message) -> {
            System.out.println(message.getMessage());
            connection.send(new Message("pog", "bigger pog"));
        });
    }
}