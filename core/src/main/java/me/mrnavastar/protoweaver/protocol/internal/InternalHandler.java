package me.mrnavastar.protoweaver.protocol.internal;

import me.mrnavastar.protoweaver.ProtoWeaver;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.netty.ProtoConnection;
import me.mrnavastar.protoweaver.protocol.Protocol;

public class InternalHandler extends ProtoPacketHandler {

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof Handshake handshake) {
            System.out.println("Got handshake!");
            Protocol newProtocol = ProtoWeaver.getLoadedProtocol(handshake.getProtocolName());
            connection.upgradeProtocol(newProtocol, newProtocol.getNewServerHandler());
        }
    }
}
