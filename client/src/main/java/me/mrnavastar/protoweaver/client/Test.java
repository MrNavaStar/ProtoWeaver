package me.mrnavastar.protoweaver.client;

import me.mrnavastar.protoweaver.protocol.protomessage.ProtoMessage;

public class Test {

    public static void main(String[] args) {
        ProtoWeaverClient client = new ProtoWeaverClient(ProtoMessage.getProtocol(), "localhost", 25565);
        client.connect();
        client.disconnect();
    }
}
