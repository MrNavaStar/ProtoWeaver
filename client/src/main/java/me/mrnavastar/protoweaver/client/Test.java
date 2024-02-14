package me.mrnavastar.protoweaver.client;

import me.mrnavastar.protoweaver.api.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.api.protocol.protomessage.ProtoMessage;

import java.util.Arrays;

public class Test {

    public static void main(String[] args) throws Exception {
        ProtoWeaverClient client = new ProtoWeaverClient("localhost", 25565);
        client.connect(ProtoMessage.getProtocol()).onConnectionEstablished(connection -> {
            connection.send(new Message("pain", "poggers"));
            connection.disconnect();
        });
    }
}
