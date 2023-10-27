package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.loader.external.FabricProxyLite;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import me.mrnavastar.protoweaver.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.protocol.protomessage.ProtoMessage;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtoWeaver;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Fabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        SSLContext.init();
        FabricLoader.getInstance().getModContainer("fabricproxy-lite").ifPresent(modContainer -> FabricProxyLite.init());

        ProtoWeaver.load(ClientAuthenticator.getServerProtocol());

        // Testing
        ProtoWeaver.load(ProtoMessage.getProtocol());
        ProtoMessage.MESSAGE_RECEIVED.register((connection, message) -> {
            System.out.println(message.getMessage());
            connection.send(new Message("pog", "bigger pog"));
        });
    }
}
