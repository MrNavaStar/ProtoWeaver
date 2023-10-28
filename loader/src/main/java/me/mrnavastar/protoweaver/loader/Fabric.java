package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.loader.protocol.velocity.FabricProxyLite;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import me.mrnavastar.protoweaver.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.protocol.protomessage.ProtoMessage;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.util.DrunkenBishop;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Fabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        SSLContext.init();
        FabricLoader.getInstance().getModContainer("fabricproxy-lite").ifPresent(modContainer -> FabricProxyLite.init());

        // Testing
        ProtoWeaver.load(ProtoMessage.getProtocol());
        ProtoMessage.MESSAGE_RECEIVED.register((connection, message) -> {
            System.out.println(message.getMessage());
            connection.send(new Message("pog", "bigger pog"));
        });

        System.out.println(DrunkenBishop.parse("37:e4:ed:2d:48:38:4a:0a:f3:45:6d:d9:17:6b:bd:9d"));
    }
}