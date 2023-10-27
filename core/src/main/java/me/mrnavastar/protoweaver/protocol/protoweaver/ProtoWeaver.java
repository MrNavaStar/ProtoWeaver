package me.mrnavastar.protoweaver.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;

import java.util.HashMap;

public class ProtoWeaver implements ProtoPacketHandler {

    protected static final HashMap<String, Protocol> loadedProtocols = new HashMap<>();
    @Getter
    protected static final Protocol protocol = ProtoBuilder.protocol("protoweaver", "internal")
            .setServerHandler(ProtoWeaver.class)
            .setClientHandler(ProtoWeaver.class)
            .addPacket(ClientAuthResponse.class)
            .addPacket(ServerAuthState.class)
            .addPacket(UpgradeProtocol.class)
            .addPacket(ProtocolResponse.class)
            .build();

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (!(packet instanceof UpgradeProtocol request)) return;

        Protocol newProtocol = loadedProtocols.get(request.getProtocol());
        if (newProtocol == null) {
            protocolNotLoaded(request.getProtocol());
            connection.disconnect();
            return;
        }

        // Only respond if handshake from client
        //if (handshake.from(Handshake.Side.CLIENT)) connection.send(new Handshake(handshake.getProtocolName(), Handshake.Side.SERVER));
        connection.upgradeProtocol(newProtocol, newProtocol.newServerHandler());
    }

    protected void protocolNotLoaded(String name) {
        System.out.println("Protocol: " + name + " is not loaded! Closing connection!");
    }

    public static void load(Protocol protocol) {
        loadedProtocols.put(protocol.getName(), protocol);
    }
}