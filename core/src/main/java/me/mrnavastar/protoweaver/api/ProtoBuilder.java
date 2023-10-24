package me.mrnavastar.protoweaver.api;

import me.mrnavastar.protoweaver.protocol.CompressionType;
import me.mrnavastar.protoweaver.protocol.Protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProtoBuilder {

    private final String name;
    private List<Class<? extends ProtoPacket>> packets = new ArrayList<>();
    private Class<? extends ProtoPacketHandler> serverHandler;
    private Class<? extends ProtoPacketHandler> clientHandler;
    private CompressionType compression = CompressionType.NONE;

    public ProtoBuilder(String name) {
        this.name = name;
    }

    public static ProtoBuilder protocol(String namespace, String name) {
        return new ProtoBuilder(namespace + ":" + name);
    }
    
    public static ProtoBuilder protocol(Protocol protocol) {
        ProtoBuilder builder = new ProtoBuilder(protocol.getName());
        builder.packets = protocol.getPackets();
        builder.serverHandler = protocol.getServerHandler();
        builder.clientHandler = protocol.getClientHandler();
        return builder;
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

    public ProtoBuilder setCompression(CompressionType type) {
        compression = type;
        return this;
    }

    public Protocol build() {
        return new Protocol(name, Collections.unmodifiableList(packets), serverHandler, clientHandler, compression);
    }
}