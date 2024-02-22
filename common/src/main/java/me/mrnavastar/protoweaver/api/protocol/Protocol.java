package me.mrnavastar.protoweaver.api.protocol;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Stores all the registered packets, settings and additional configuration of a {@link ProtoWeaver} protocol.
 */
public class Protocol {

    @Getter private final String namespace;
    @Getter private final String name;
    private final Kryo kryo = new Kryo();
    @Getter private CompressionType compression = CompressionType.NONE;
    @Getter private int compressionLevel = -37;
    @Getter private int maxPacketSize = 16384;

    @Getter private Class<? extends ProtoConnectionHandler> serverHandler;
    @Getter private Class<? extends ProtoConnectionHandler> clientHandler;
    @Getter private Class<? extends ServerAuthHandler> serverAuthHandler;
    @Getter private Class<? extends ClientAuthHandler> clientAuthHandler;

    private int packetHash = 1;

    private Protocol(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    }

    /**
     * <p>Creates a new protocol builder. A good rule of thumb for naming that ensures maximum compatibility is to use
     * your mod id or project id for the namespace and to give the name something unique.</p>
     * <br>For example: "protoweaver:proto-message"</br>
     * @param namespace Usually should be set to your mod id or project id
     * @param name The name of your protocol.
     */
    public static Builder create(@NonNull String namespace, @NonNull String name) {
        return new Builder(new Protocol(namespace, name));
    }

    /**
     * Allows you to create modify an existing {@link Protocol}. The {@link Protocol} object returned from
     * {@link Builder#build()} will be the same object as the one that this method was called on (not a copy). In
     * theory this means you can modify a protocol without reloading it, or while its currently active. Here be dragons,
     * so use with caution.
     */
    public Builder modify() {
        return new Builder(this);
    }

    public byte[] serialize(Object packet) {
        try (Output output = new Output(maxPacketSize)) {
            try {
                kryo.writeClassAndObject(output, packet);
            } catch (IllegalArgumentException ignore) {}
            return output.toBytes();
        }
    }

    public Object deserialize(byte[] packet) {
        try (Input in = new Input(packet)) {
            return kryo.readClassAndObject(in);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                namespace, name,
                packetHash,
                compression.ordinal(), compressionLevel,
                maxPacketSize
        );
    }

    @Override
    public String toString() {
        return namespace + ":" + name;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private final Protocol protocol;

        /**
         * Set the packet handler that the server will use to process inbound packets.
         * @param handler The class of the packet handler.
         */
        public Builder setServerHandler(Class<? extends ProtoConnectionHandler> handler) {
            protocol.serverHandler = handler;
            return this;
        }

        /**
         * Set the packet handler that the client will use to process inbound packets.
         * @param handler The class of the packet handler.
         */
        public Builder setClientHandler(Class<? extends ProtoConnectionHandler> handler) {
            protocol.clientHandler = handler;
            return this;
        }

        /**
         * Set the auth handler that the server will use to process inbound client secrets.
         * @param handler The class of the auth handler.
         */
        public Builder setServerAuthHandler(Class<? extends ServerAuthHandler> handler) {
            protocol.serverAuthHandler = handler;
            return this;
        }

        /**
         * Set the auth handler that the client will use to get the secret that will be sent to the server.
         * @param handler The class of the auth handler.
         */
        public Builder setClientAuthHandler(Class<? extends ClientAuthHandler> handler) {
            protocol.clientAuthHandler = handler;
            return this;
        }

        /**
         * Register a class to the {@link Protocol}. Does nothing if the class has already been registered.
         * @param packet The packet to register.
         */
        public Builder addPacket(@NonNull Class<?> packet) {
            if (protocol.kryo.getClassResolver().getRegistration(packet) != null) return this;

            try {
                protocol.kryo.register(packet);
                protocol.packetHash = 31 * protocol.packetHash + packet.getName().hashCode();

                for (Field field : packet.getDeclaredFields())
                    if (((Modifier.STATIC | Modifier.TRANSIENT) & field.getModifiers()) == 0) addPacket(field.getType());
            } catch (InaccessibleObjectException ignore) {}
            return this;
        }

        /**
         * Enables compression on the {@link Protocol}. The compression type by defaults is set to {@link CompressionType#NONE}.
         * @param type The type of compression to enable.
         */
        public Builder setCompression(@NonNull CompressionType type) {
            protocol.compression = type;
            return this;
        }

        /**
         * Set the compression level if compression is enabled. Be sure to check the supported level for each type of
         * compression online.
         * @param level The compression level to set.
         */
        public Builder setCompressionLevel(int level) {
            protocol.compressionLevel = level;
            return this;
        }

        /**
         * Set the maximum packet size this {@link Protocol} can handle. The higher the value, the more ram will be
         * allocated when sending and receiving packets. The maximum packet size defaults to 16kb.
         * @param maxPacketSize The maximum size a packet can be in bytes
         */
        public Builder setMaxPacketSize(int maxPacketSize) {
            protocol.maxPacketSize = maxPacketSize;
            return this;
        }

        /**
         * Build the {@link Protocol}.
         * @return A finished protocol that can be loaded using {@link ProtoWeaver#load(Protocol)}.
         */
        public Protocol build() {
            if (protocol.compression != CompressionType.NONE && protocol.compressionLevel == -37)
                protocol.compressionLevel = protocol.compression.getDefaultLevel();
            return protocol;
        }
    }
}