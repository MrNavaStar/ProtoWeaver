package me.mrnavastar.protoweaver.api;

import me.mrnavastar.protoweaver.netty.ProtoConnection;

public interface ProtoPacketHandler {
    void ready(ProtoConnection connection);
    void handlePacket(ProtoConnection connection, ProtoPacket packet);
}