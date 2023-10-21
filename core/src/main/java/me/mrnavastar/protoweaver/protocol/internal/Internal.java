package me.mrnavastar.protoweaver.protocol.internal;

import lombok.Getter;
import me.mrnavastar.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaverAPI;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;

public class Internal extends ProtoPacketHandler {

    @Getter
    private static final Protocol protocol = ProtoWeaverAPI.buildProtocol("protoweaver", "internal").setServerHandler(Internal.class).addPacket(Handshake.class).build();

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof Handshake handshake) {
            System.out.println("HANDSHAKE: " + handshake.getProtocolName());

            Protocol newProtocol = ProtoWeaver.getLoadedProtocol(handshake.getProtocolName());
            connection.upgradeProtocol(newProtocol, newProtocol.getNewServerHandler());
            System.out.println(connection.getProtocol().getName());
        }
    }
}
