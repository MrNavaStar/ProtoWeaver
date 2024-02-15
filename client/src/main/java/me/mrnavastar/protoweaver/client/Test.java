package me.mrnavastar.protoweaver.client;

import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.protomessage.Message;
import me.mrnavastar.protoweaver.api.protocol.protomessage.ProtoMessage;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;

public class Test {

    public static void main(String[] args) {

        Protocol p = Protocol.create("protoweaver","proto-message")
                .enableCompression(CompressionType.SNAPPY)
                .setServerHandler(ProtoMessage.class)
                .setClientHandler(ProtoMessage.class)
                .addPacket(Message.class)
                .addPacket(VelocityAuth.class)
                .build();

        ProtoWeaverClient client = new ProtoWeaverClient("localhost", 25565);
        client.connect(p).onConnectionEstablished(connection -> {
            connection.send(new Message("pain", "poggers"));
            connection.disconnect();
        });
    }
}
