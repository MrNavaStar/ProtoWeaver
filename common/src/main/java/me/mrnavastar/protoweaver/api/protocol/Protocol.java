package me.mrnavastar.protoweaver.api.protocol;

import lombok.*;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Protocol {

    private final String name;
    private final List<Class<? extends ProtoPacket>> packets;
    private final Class<? extends ProtoConnectionHandler> serverHandler;
    private final Class<? extends ProtoConnectionHandler> clientHandler;
    private final Class<? extends ServerAuthHandler> serverAuthHandler;
    private final Class<? extends ClientAuthHandler> clientAuthHandler;
    private final CompressionType compression;
    private final int compressionLevel;

    @SneakyThrows
    public ProtoConnectionHandler newHandler(@NonNull Side side) throws NoSuchMethodException {
        return switch (side) {
            case CLIENT -> clientHandler.getDeclaredConstructor().newInstance();
            case SERVER -> serverHandler.getDeclaredConstructor().newInstance();
        };
    }

    @SneakyThrows
    public ServerAuthHandler newServerAuthHandler() {
        return serverAuthHandler.getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    public ClientAuthHandler newClientAuthHandler() {
        return clientAuthHandler.getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    public ProtoPacket getPacket(int packetID) {
        if (packetID < 0 || packetID >= packets.size()) return null;
        return packets.get(packetID).getDeclaredConstructor().newInstance();
    }

    public int getPacketId(@NonNull ProtoPacket packet) {
        return packets.indexOf(packet.getClass());
    }

    @Override
    public String toString() {
        return name;
    }
}