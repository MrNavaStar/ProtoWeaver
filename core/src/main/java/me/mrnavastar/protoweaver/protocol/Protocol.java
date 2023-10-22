package me.mrnavastar.protoweaver.protocol;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;

import java.util.ArrayList;

public class Protocol {

    @Getter
    private final String name;
    private final ArrayList<Class<? extends ProtoPacket>> packets;
    @Setter
    private Class<? extends ProtoPacketHandler> serverHandler;
    @Setter
    private Class<? extends ProtoPacketHandler> clientHandler;

    public Protocol(String name, ArrayList<Class<? extends ProtoPacket>> packets, Class<? extends ProtoPacketHandler> serverHandler, Class<? extends ProtoPacketHandler> clientHandler) {
        this.name = name;
        this.packets = packets;
        this.serverHandler = serverHandler;
        this.clientHandler = clientHandler;
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
        Class<? extends ProtoPacket> packetClass = packets.get(packetID);
        if (packetClass == null) return null;

        return packetClass.getDeclaredConstructor().newInstance();
    }

    public int getPacketId(ProtoPacket packet) {
        return packets.indexOf(packet.getClass());
    }
}