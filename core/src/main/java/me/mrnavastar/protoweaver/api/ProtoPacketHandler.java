package me.mrnavastar.protoweaver.api;

import me.mrnavastar.protoweaver.netty.ProtoConnection;

public interface ProtoPacketHandler {
    void handlePacket(ProtoConnection connection, ProtoPacket packet);
}