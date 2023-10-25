package me.mrnavastar.protoweaver.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;

import java.util.HashMap;

public class ProtoWeaver implements ProtoPacketHandler {

    private static final HashMap<String, Protocol> loadedProtocols = new HashMap<>();
    @Getter
    private static final Protocol protocol = ProtoBuilder.protocol("protoweaver", "internal")
            .setServerHandler(ProtoWeaver.class)
            .setClientHandler(ProtoWeaver.class)
            .addPacket(Handshake.class)
            .build();

    static {
        load(protocol);
    }

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (!(packet instanceof Handshake handshake)) return;

        Protocol newProtocol = loadedProtocols.get(handshake.getProtocolName());
        if (newProtocol == null) {
            protocolNotLoaded(handshake.getProtocolName());
            connection.disconnect();
            return;
        }

        // Only respond if handshake from client
        if (handshake.from(Handshake.Side.CLIENT)) connection.send(new Handshake(handshake.getProtocolName(), Handshake.Side.SERVER));
        connection.upgradeProtocol(newProtocol, newProtocol.newServerHandler());
    }

    private void protocolNotLoaded(String name) {
        System.out.println("Protocol: " + name + " is not loaded! Closing connection!");
    }

    public static void load(Protocol protocol) {
        loadedProtocols.put(protocol.getName(), protocol);
    }
}