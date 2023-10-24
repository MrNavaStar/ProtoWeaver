package me.mrnavastar.protoweaver.protocol.protomessage;

import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.util.Event;

public class ProtoMessageEvents {

    public static final Event<MessageReceived> MESSAGE_RECEIVED = new Event<>(callbacks -> (connection, message) -> {
        callbacks.forEach(callback -> callback.trigger(connection, message));
    });

    @FunctionalInterface
    public interface MessageReceived {
        void trigger(ProtoConnection connection, Message message);
    }
}