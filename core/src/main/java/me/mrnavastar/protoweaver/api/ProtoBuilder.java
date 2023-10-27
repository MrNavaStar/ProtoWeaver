package me.mrnavastar.protoweaver.api;

import io.netty.handler.codec.compression.Brotli;
import lombok.NonNull;
import me.mrnavastar.protoweaver.protocol.CompressionType;
import me.mrnavastar.protoweaver.protocol.Protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProtoBuilder {

    private final String name;
    private List<Class<? extends ProtoPacket>> packets = new ArrayList<>();
    private Class<? extends ProtoPacketHandler> serverHandler = null;
    private Class<? extends ProtoPacketHandler> clientHandler = null;
    private Class<? extends ServerAuthHandler> serverAuthHandler = null;
    private Class<? extends ClientAuthHandler> clientAuthHandler = null;
    private CompressionType compression = CompressionType.NONE;
    private int compressionLevel = -37;

    public ProtoBuilder(String name) {
        this.name = name;
    }

    public static ProtoBuilder protocol(@NonNull String namespace, @NonNull String name) {
        return new ProtoBuilder(namespace + ":" + name);
    }
    
    public static ProtoBuilder protocol(@NonNull Protocol protocol) {
        ProtoBuilder builder = new ProtoBuilder(protocol.getName());
        builder.packets = protocol.getPackets();
        builder.serverHandler = protocol.getServerHandler();
        builder.clientHandler = protocol.getClientHandler();
        return builder;
    }

    public ProtoBuilder setServerHandler(@NonNull Class<? extends ProtoPacketHandler> packetHandler) {
        this.serverHandler = packetHandler;
        return this;
    }

    public ProtoBuilder setClientHandler(@NonNull Class<? extends ProtoPacketHandler> packetHandler) {
        this.clientHandler = packetHandler;
        return this;
    }

    public ProtoBuilder setServerAuthHandler(@NonNull Class<? extends ServerAuthHandler> handler) {
        this.serverAuthHandler = handler;
        return this;
    }

    public ProtoBuilder setClientAuthHandler(@NonNull Class<? extends ClientAuthHandler> handler) {
        this.clientAuthHandler = handler;
        return this;
    }

    public <T extends ProtoPacket> ProtoBuilder addPacket(@NonNull Class<T> packet) {
        packets.add(packet);
        return this;
    }

    public ProtoBuilder enableCompression(@NonNull CompressionType type) {
        switch (compression) {
            case BROTLI -> {
                if (Brotli.isAvailable()) break;
                // Explain that brotli is missing
                System.out.println("Brotli not found!");
                System.exit(1);
            }
            case LZ4 -> {
                try {
                    Class.forName("net.jpountz.lz4.LZ4Compressor");
                } catch (ClassNotFoundException e) {
                    // Explain that LZ4 is missing
                    System.out.println("LZ4 not found!");
                    System.exit(1);
                }
            }
        }

        compression = type;
        return this;
    }

    public ProtoBuilder setCompressionLevel(int level) {
        compressionLevel = level;
        return this;
    }

    public Protocol build() {
        if (compression != CompressionType.NONE && compressionLevel == -37) compressionLevel = compression.getDefaultLevel();
        return new Protocol(name, Collections.unmodifiableList(packets), serverHandler, clientHandler, serverAuthHandler, clientAuthHandler, compression, compressionLevel);
    }
}