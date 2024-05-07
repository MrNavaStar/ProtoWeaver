package me.mrnavastar.protoweaver.api;

import lombok.NonNull;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.util.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoWeaver {

    private static final ConcurrentHashMap<String, Protocol> loadedProtocols = new ConcurrentHashMap<>();

    /**
     * Loads the given {@link Protocol}. Does nothing if this {@link Protocol} (one with a matching name + namespace) has already been loaded.
     */
    public static void load(@NonNull Protocol protocol) {
        if (loadedProtocols.containsKey(protocol.toString())) return;

        Event.Cancelable cancelable = new Event.Cancelable();
        PRE_PROTOCOL_LOADED.getInvoker().trigger(protocol, cancelable);
        if (cancelable.isCanceled()) return;

        loadedProtocols.put(protocol.toString(), protocol);
        PROTOCOL_LOADED.getInvoker().trigger(protocol);
    }

    /**
     * @return An immutable list of the loaded {@link Protocol}s in the current jvm instance
     */
    public static List<Protocol> getLoadedProtocols() {
        return loadedProtocols.values().stream().toList();
    }

    /**
     * Get the {@link Protocol} registered under a namespace and name.
     *
     * @param namespaceAndName The namespace and name of the protocol joined with a colon: "namespace:name"
     * @return The registered {@link Protocol}. Will be null if no protocol was found.
     */
    public static Protocol getLoadedProtocol(@NonNull String namespaceAndName) {
        return loadedProtocols.get(namespaceAndName);
    }

    /**
     * Get the {@link Protocol} registered under a namespace and name.
     *
     * @param namespace The namespace of the protocol
     * @param name The name of the protocol
     * @return The registered {@link Protocol}. Will be null if no protocol was found.
     */
    public static Protocol getLoadedProtocol(@NonNull String namespace, @NonNull String name) {
        return getLoadedProtocol(namespace + ":" + name);
    }

    /**
     * An event that is fired when {@link ProtoWeaver#load(Protocol)} is called. This event can be used to cancel the loading
     * of a protocol by calling {@link Event.Cancelable#cancel()}. You can't cancel the internal protoweaver protocol.
     */
    public static final Event<PreLoadedProtocol> PRE_PROTOCOL_LOADED = new Event<>(callbacks -> (protocol, cancelable) -> {
        for (PreLoadedProtocol callback : callbacks) {
            if (cancelable.isCanceled() && !protocol.getNamespace().equals("protoweaver") && !protocol.getName().equals("internal")) break;
            callback.trigger(protocol, cancelable);
        }
    });

    /**
     * An event that is fired once a {@link Protocol} has been fully loaded.
     */
    public static final Event<LoadedProtocol> PROTOCOL_LOADED = new Event<>(callbacks -> protocol -> {
        callbacks.forEach(callback -> callback.trigger(protocol));
    });

    @FunctionalInterface
    public interface PreLoadedProtocol {
        void trigger(Protocol protocol, Event.Cancelable cancelable);
    }

    @FunctionalInterface
    public interface LoadedProtocol {
        void trigger(Protocol protocol);
    }
}