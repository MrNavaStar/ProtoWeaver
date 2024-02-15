package me.mrnavastar.protoweaver.api;

import lombok.NonNull;
import me.mrnavastar.protoweaver.api.protocol.Protocol;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoWeaver {

    private static final ConcurrentHashMap<String, Protocol> loadedProtocols = new ConcurrentHashMap<>();

    /**
     * Loads the given {@link Protocol}. Does nothing if this {@link Protocol} (or one with a matching name + namespace) has already been loaded.
     */
    public static void load(@NonNull Protocol protocol) {
        if (loadedProtocols.containsKey(protocol.toString())) return;
        loadedProtocols.put(protocol.toString(), protocol);
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
}