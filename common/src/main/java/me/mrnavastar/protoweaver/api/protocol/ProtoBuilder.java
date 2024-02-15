package me.mrnavastar.protoweaver.api.protocol;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

/**
 * A simple builder class for constructing {@link Protocol}'s
 */
@RequiredArgsConstructor
public class ProtoBuilder {

    private final String name;
    private final Kryo kryo = new Kryo();
    private Class<? extends ProtoConnectionHandler> serverHandler = null;
    private Class<? extends ProtoConnectionHandler> clientHandler = null;
    private Class<? extends ServerAuthHandler> serverAuthHandler = null;
    private Class<? extends ClientAuthHandler> clientAuthHandler = null;
    private CompressionType compression = CompressionType.NONE;
    private int compressionLevel = -37;
    private int maxPacketSize = 16384; // 16kb

    /**
     * <p>Creates a new protocol builder. A good rule of thumb for naming that ensures maximum compatibility is to use your
     * mod or project id for the namespace and to give the name something unique.</p>
     * <br>For example: protoweaver:proto-message</br>
     * @param namespace Usually should be set to your mod or project id
     * @param name The name of your protocol.
     * @return {@link ProtoBuilder}
     */
    public static ProtoBuilder protocol(@NonNull String namespace, @NonNull String name) {
        return new ProtoBuilder(namespace + ":" + name);
    }

    /**
     * Set the packet handler that the server will use to process inbound packets.
     * @param packetHandler The class of the packet handler.
     * @return {@link ProtoBuilder}
     */
    public ProtoBuilder setServerHandler(@NonNull Class<? extends ProtoConnectionHandler> packetHandler) {
        this.serverHandler = packetHandler;
        return this;
    }

    /**
     * Set the packet handler that the client will use to process inbound packets.
     * @param packetHandler The class of the packet handler.
     * @return {@link ProtoBuilder}
     */
    public ProtoBuilder setClientHandler(@NonNull Class<? extends ProtoConnectionHandler> packetHandler) {
        this.clientHandler = packetHandler;
        return this;
    }

    /**
     * Set the auth handler that the server will use to process inbound client secrets.
     * @param handler The class of the auth handler.
     * @return {@link ProtoBuilder}
     */
    public ProtoBuilder setServerAuthHandler(@NonNull Class<? extends ServerAuthHandler> handler) {
        this.serverAuthHandler = handler;
        return this;
    }

    /**
     * Set the auth handler that the client will use to get the secret that will be sent to the server.
     * @param handler The class of the auth handler.
     * @return {@link ProtoBuilder}
     */
    public ProtoBuilder setClientAuthHandler(@NonNull Class<? extends ClientAuthHandler> handler) {
        this.clientAuthHandler = handler;
        return this;
    }

    /**
     * Register a packet to the protocol.
     * @param packet The packet to register.
     * @return {@link ProtoBuilder}
     */
    public ProtoBuilder addPacket(@NonNull Class<?> packet) {
        if (kryo.getClassResolver().getRegistration(packet) != null) return this;

        try {
            kryo.register(packet);
            for (Field field : packet.getDeclaredFields()) addPacket(field.getType());
        } catch (InaccessibleObjectException ignore) {}
        return this;
    }

    /**
     * Enables compression on the protocol.
     * @param type The type of compression to enable. Defaults to NONE.
     * @return {@link ProtoBuilder}
     */
    public ProtoBuilder enableCompression(@NonNull CompressionType type) {
        compression = type;
        return this;
    }

    /**
     * Set the compression level if compression is enabled. Be sure to check the supported level for each protocol online.
     * @param level The compression level to set.
     * @return {@link ProtoBuilder}
     */
    public ProtoBuilder setCompressionLevel(int level) {
        compressionLevel = level;
        return this;
    }

    /**
     * Set the maximum packet size this protocol can handle. The higher the value, the more ram will be allocated when sending and receiving packets.
     * The maximum packet size defaults to 16kb
     * @param maxPacketSize The maximum size a packet can be in bytes
     * @return {@link ProtoBuilder}
     */
    public ProtoBuilder setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
        return this;
    }

    /**
     * Build the protocol.
     * @return A finished protocol that can be loaded using {@link ProtoWeaver#load(Protocol)}.
     */
    public Protocol build() {
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        if (compression != CompressionType.NONE && compressionLevel == -37) compressionLevel = compression.getDefaultLevel();
        return new Protocol(name, kryo, compression, compressionLevel, maxPacketSize, serverHandler, clientHandler, serverAuthHandler, clientAuthHandler);
    }
}