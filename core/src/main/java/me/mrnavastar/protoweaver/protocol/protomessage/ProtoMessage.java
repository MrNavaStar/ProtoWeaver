package me.mrnavastar.protoweaver.protocol.protomessage;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;

public class ProtoMessage implements ProtoPacketHandler {

    @Getter
    private static final Protocol protocol = ProtoBuilder.protocol("protoweaver", "proto-message").setServerHandler(ProtoMessage.class).setClientHandler(ProtoMessage.class).addPacket(Message.class).build();

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof Message message) ProtoMessageEvents.MESSAGE_RECEIVED.getInvoker().trigger(connection, message);
    }
}