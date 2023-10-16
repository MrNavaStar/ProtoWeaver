package me.mrnavastar.protoweaver.protocol.protomessage;

import me.mrnavastar.protoweaver.util.Event;

public class ProtoMessageEvents {

    public static final Event<MessageReceived> MESSAGE_RECEIVED = new Event<>(callbacks -> (message) -> {
        callbacks.forEach(callback -> callback.trigger(message));
    });

    @FunctionalInterface
    public interface MessageReceived {
        void trigger(Message message);
    }
}