package me.mrnavastar.protoweaver.client;

import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.api.protocol.protomessage.ProtoMessage;

public class Test {

    public static void main(String[] args) {

        ProtoMessage.MESSAGE_RECEIVED.register((connection, channel, message) -> {
            System.out.println(message);
        });

        ProtoWeaver.load(ProtoMessage.getProtocol());

        ProtoWeaverClient client = new ProtoWeaverClient("localhost", 25565);
        client.connect(ProtoMessage.getProtocol());

        client.send(new Message("pog", "champ"));
    }
}