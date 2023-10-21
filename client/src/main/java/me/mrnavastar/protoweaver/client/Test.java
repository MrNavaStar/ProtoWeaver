package me.mrnavastar.protoweaver.client;

import me.mrnavastar.protoweaver.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.protocol.protomessage.ProtoMessage;

public class Test {

    public static void main(String[] args) {

        System.out.println("Starting");

        ProtoClient client = new ProtoClient(ProtoMessage.getProtocol(), "localhost", 25565);
        client.connect();

        client.send(new Message("pain", "this be poggers"));
    }
}
