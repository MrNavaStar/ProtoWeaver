package me.mrnavastar.protoweaver.api.protocol.protomessage;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.util.Event;

/**
 * Serves mostly as an example protocol, however it can be used in your mod if your so desire.
 */
public class ProtoMessage implements ProtoPacketHandler {

    @Getter
    private static final Protocol protocol = ProtoBuilder.protocol("protoweaver", "proto-message")
            .enableCompression(CompressionType.SNAPPY)
            .setServerHandler(ProtoMessage.class)
            .setClientHandler(ProtoMessage.class)
            .addPacket(Message.class)
            .build();

    @Override
    public void ready(ProtoConnection connection) {}

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof Message message) MESSAGE_RECEIVED.getInvoker().trigger(connection, message);
    }

    /**
     * This event is triggered when a message is received and can be used both on the server and the client.
     * Be sure to load this protocol.
     */
    public static final Event<MessageReceived> MESSAGE_RECEIVED = new Event<>(callbacks -> (connection, message) -> {
        callbacks.forEach(callback -> callback.trigger(connection, message));
    });

    @FunctionalInterface
    public interface MessageReceived {
        void trigger(ProtoConnection connection, Message message);
    }
}