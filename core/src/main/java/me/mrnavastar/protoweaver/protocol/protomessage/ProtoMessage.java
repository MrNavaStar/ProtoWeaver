package me.mrnavastar.protoweaver.protocol.protomessage;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;

public class ProtoMessage extends ProtoPacketHandler {

    @Getter
    private static final Protocol protocol = ProtoBuilder.protocol("protoweaver", "proto-message").setServerHandler(ProtoMessage.class).setClientHandler(ProtoMessage.class).addPacket(Message.class).build();

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof Message message) {
            System.out.println("MESSAGE: " + message.getMessage());
            ProtoMessageEvents.MESSAGE_RECEIVED.getInvoker().trigger(message);
        }
    }
}