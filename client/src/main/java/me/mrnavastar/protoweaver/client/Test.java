package me.mrnavastar.protoweaver.client;

import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.api.protocol.protomessage.ProtoMessage;

public class Test {

    public static void main(String[] args) throws InterruptedException {

        ProtoMessage.MESSAGE_RECEIVED.register((connection, channel, message) -> {
            System.out.println(message);
        });

        ProtoWeaver.load(ProtoMessage.getProtocol());

        ProtoWeaverClient client = new ProtoWeaverClient("localhost", 25565);
        client.connectForever(ProtoMessage.getProtocol()).whenCompleteAsync((aBoolean, throwable) -> {

        });


        System.out.println("sending");
        client.disconnect();
       // client.send(new Message("pog", "champ"));
    }
}