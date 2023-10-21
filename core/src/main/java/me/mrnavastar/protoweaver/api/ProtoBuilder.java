package me.mrnavastar.protoweaver.api;

import lombok.Getter;
import me.mrnavastar.protoweaver.protocol.Protocol;

import java.util.ArrayList;

@Getter
public class ProtoBuilder {

    private final String name;
    private final ArrayList<Class<? extends ProtoPacket>> packets = new ArrayList<>();
    private Class<? extends ProtoPacketHandler> serverHandler;
    private Class<? extends ProtoPacketHandler> clientHandler;

    public ProtoBuilder(String name) {
        this.name = name;
    }

    public ProtoBuilder setServerHandler(Class<? extends ProtoPacketHandler> packetHandler) {
        this.serverHandler = packetHandler;
        return this;
    }

    public ProtoBuilder setClientHandler(Class<? extends ProtoPacketHandler> packetHandler) {
        this.clientHandler = packetHandler;
        return this;
    }

    public <T extends ProtoPacket> ProtoBuilder addPacket(Class<T> packet) {
        packets.add(packet);
        return this;
    }

    public Protocol build() {
        return new Protocol(name, packets, serverHandler, clientHandler);
    }
}