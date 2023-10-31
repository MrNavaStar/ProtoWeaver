package me.mrnavastar.protoweaver.api;

import lombok.NonNull;
import me.mrnavastar.protoweaver.api.protocol.Protocol;

import java.util.HashMap;

public class ProtoWeaver {

    protected static final HashMap<String, Protocol> loadedProtocols = new HashMap<>();

    /**
     * Loads the given protocol.
     */
    public static void load(@NonNull Protocol protocol) {
        loadedProtocols.put(protocol.getName(), protocol);
    }
}