package me.mrnavastar.protoweaver.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;

import java.util.HashMap;

public class ProtoWeaver extends ProtoPacketHandler {

    @Getter
    private static final Protocol protocol = ProtoBuilder.protocol("protoweaver", "internal").setServerHandler(ProtoWeaver.class).setClientHandler(ProtoWeaver.class).addPacket(Handshake.class).build();
    private static final HashMap<String, Protocol> loadedProtocols = new HashMap<>();

    public static void initServer() {
        load(protocol);
    }

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof Handshake handshake) {
            Protocol newProtocol = loadedProtocols.get(handshake.getProtocolName());
            if (protocol == null) {
                protocolNotLoaded(handshake.getProtocolName());
                return;
            }

            connection.upgradeProtocol(newProtocol, newProtocol.newServerHandler());
        }
    }

    private void protocolNotLoaded(String name) {

    }

    public static void load(Protocol protocol) {
        loadedProtocols.put(protocol.getName(), protocol);
    }
}