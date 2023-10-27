package me.mrnavastar.protoweaver.client;

import me.mrnavastar.protoweaver.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.protocol.protomessage.ProtoMessage;

public class Test {

    public static void main(String[] args) {

        ProtoMessage.MESSAGE_RECEIVED.register((connection, message) -> {
            System.out.println(message.getMessage());
        });

        ProtoWeaverClient client = new ProtoWeaverClient(ProtoMessage.getProtocol(), "localhost", 25565);
        client.connect();

        client.send(new Message("pog", "champ"));
    }
}