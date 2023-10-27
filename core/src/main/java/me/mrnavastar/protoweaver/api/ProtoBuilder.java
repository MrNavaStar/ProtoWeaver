package me.mrnavastar.protoweaver.api;

import io.netty.handler.codec.compression.Brotli;
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
    private Class<? extends ProtoAuthHandler> authHandler = null;
    private CompressionType compression = CompressionType.NONE;
    private int compressionLevel = -37;

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

    public ProtoBuilder setAuthHandler(Class<? extends ProtoAuthHandler> handler) {
        this.authHandler = handler;
        return this;
    }

    public <T extends ProtoPacket> ProtoBuilder addPacket(Class<T> packet) {
        packets.add(packet);
        return this;
    }

    public ProtoBuilder enableCompression(CompressionType type) {
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
        if (compression != CompressionType.NONE) compressionLevel = compression.getDefaultLevel();
        return new Protocol(name, Collections.unmodifiableList(packets), serverHandler, clientHandler, authHandler, compression, compressionLevel);
    }
}