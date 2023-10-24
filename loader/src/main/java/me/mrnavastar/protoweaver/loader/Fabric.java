package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import me.mrnavastar.protoweaver.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.protocol.protomessage.ProtoMessage;
import me.mrnavastar.protoweaver.protocol.protoweaver.ProtoWeaver;
import net.fabricmc.api.DedicatedServerModInitializer;

public class Fabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        SSLContext.init();

        ProtoWeaver.load(ProtoMessage.getProtocol());
        ProtoMessage.MESSAGE_RECEIVED.register((connection, message) -> {
            System.out.println(message.getMessage());
            connection.send(new Message("pog", "bigger pog"));
        });
    }
}
