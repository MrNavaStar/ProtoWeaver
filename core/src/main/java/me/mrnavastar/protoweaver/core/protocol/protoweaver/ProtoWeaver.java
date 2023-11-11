package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.extern.java.Log;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

@Log
public class ProtoWeaver extends me.mrnavastar.protoweaver.api.ProtoWeaver {

    protected static final Protocol baseProtocol = ProtoBuilder.protocol("protoweaver", "internal")
            .addPacket(ClientSecret.class)
            .addPacket(AuthStatus.class)
            .addPacket(ProtocolStatus.class)
            .build();

    protected void protocolNotLoaded(String name) {
        ProtoLogger.warn("Protocol: " + name + " is not loaded! Closing connection!");
    }
}