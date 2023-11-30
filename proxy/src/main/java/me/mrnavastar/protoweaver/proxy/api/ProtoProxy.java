package me.mrnavastar.protoweaver.proxy.api;

import lombok.NonNull;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.Protocol;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoProxy {


    protected static final ConcurrentHashMap<String, Protocol> loadedProtocols = new ConcurrentHashMap<>();

    /**
     * Loads the given protocol.
     */
    public static void load(@NonNull Protocol protocol) {
        ProtoWeaver.load(protocol);
        loadedProtocols.put(protocol.getName(), protocol);
    }

    /**
     * @return An immutable list of the loaded protocols in the current jvm instance
     */
    public static List<Protocol> getLoadedProtocols() {
        return loadedProtocols.values().stream().toList();
    }
}
