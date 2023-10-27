package me.mrnavastar.protoweaver.protocol.protoweaver;

import lombok.NonNull;
import lombok.extern.java.Log;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.protocol.Protocol;

import java.util.HashMap;

@Log
public class ProtoWeaver {

    protected static final HashMap<String, Protocol> loadedProtocols = new HashMap<>();
    protected static final Protocol baseProtocol = ProtoBuilder.protocol("protoweaver", "internal")
            .addPacket(ClientSecret.class)
            .addPacket(AuthStatus.class)
            .addPacket(ProtocolStatus.class)
            .build();

    protected void protocolNotLoaded(String name) {
        log.warning("Protocol: " + name + " is not loaded! Closing connection!");
    }

    public static void load(@NonNull Protocol protocol) {
        loadedProtocols.put(protocol.getName(), protocol);
    }
}