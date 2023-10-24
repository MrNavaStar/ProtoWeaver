package me.mrnavastar.protoweaver.protocol;

import lombok.Getter;
import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;

import java.util.List;

@Getter
public class Protocol {
    
    private final String name;
    private final List<Class<? extends ProtoPacket>> packets;
    private final Class<? extends ProtoPacketHandler> serverHandler;
    private final Class<? extends ProtoPacketHandler> clientHandler;
    private final CompressionType compression;

    public Protocol(String name, List<Class<? extends ProtoPacket>> packets, Class<? extends ProtoPacketHandler> serverHandler, Class<? extends ProtoPacketHandler> clientHandler, CompressionType compression) {
        this.name = name;
        this.packets = packets;
        this.serverHandler = serverHandler;
        this.clientHandler = clientHandler;
        this.compression = compression;
    }

    @SneakyThrows
    public ProtoPacketHandler newClientHandler() {
        return clientHandler.getConstructor().newInstance();
    }

    @SneakyThrows
    public ProtoPacketHandler newServerHandler() {
        return serverHandler.getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    public ProtoPacket getPacket(int packetID) {
        if (packetID < 0 || packetID >= packets.size()) return null;
        return packets.get(packetID).getDeclaredConstructor().newInstance();
    }

    public int getPacketId(ProtoPacket packet) {
        return packets.indexOf(packet.getClass());
    }
}