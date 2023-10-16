package me.mrnavastar.protoweaver;

import me.mrnavastar.protoweaver.api.*;
import me.mrnavastar.protoweaver.protocol.Protocol;
import me.mrnavastar.protoweaver.protocol.internal.Handshake;
import me.mrnavastar.protoweaver.protocol.internal.InternalHandler;


import java.util.HashMap;

public class ProtoWeaver extends ProtoWeaverAPI {

    private static final HashMap<String, Protocol> loadedProtocols = new HashMap<>();

    public static void init() {
        registerBuiltInProtocols();
    }

    public static void loadProtocol(Protocol protocol) {
        loadedProtocols.put(protocol.getName(), protocol);
    }

    public static Protocol getLoadedProtocol(String name) {
        return loadedProtocols.get(name);
    }

    public static void gotPacketFromUnregisteredProtocol(String protocol) {


        // Explain that the protocol is not loaded on the server, head to meta.link for more info
    }

    public static void gotUnknownPacket(Protocol protocol, int packetId) {
        // Be mad idk
    }

    public static void failedToDecodePacket(Protocol protocol, int packetId) {
        // Be more mad
    }

    public static void registerBuiltInProtocols() {
        Protocol protocol = ProtoWeaverAPI.buildProtocol("protoweaver", "internal")
                .setServerHandler(InternalHandler.class)
                .addPacket(Handshake.class)
                .build();

        loadProtocol(protocol);
    }
}