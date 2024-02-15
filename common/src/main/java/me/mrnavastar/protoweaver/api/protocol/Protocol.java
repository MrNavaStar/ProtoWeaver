package me.mrnavastar.protoweaver.api.protocol;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy;
import lombok.*;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
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

    private Protocol(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    }

    /**
     * <p>Creates a new protocol builder. A good rule of thumb for naming that ensures maximum compatibility is to use your
     * mod id or project id for the namespace and to give the name something unique.</p>
     * <br>For example: "protoweaver:proto-message"</br>
     * @param namespace Usually should be set to your mod id or project id
     * @param name The name of your protocol.
     */
    public static Builder create(@NonNull String namespace, @NonNull String name) {
        return new Builder(new Protocol(namespace, name));
    }

    /**
     * Allows you to create modify an existing protocol. The {@link Protocol} object returned from {@link Builder#build()} will
     * be the same object as the one that this method was called on (not a copy). In practice, this means you can modify
     * a protocol without reloading it with {@link ProtoWeaver#load(Protocol)}
     */
    public Builder modify() {
        return new Builder(this);
    }

    public byte[] serialize(Object packet) {
        try (Output output = new Output(maxPacketSize)) {
            kryo.writeClassAndObject(output, packet);
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
                // TODO: Make packet order effect the hashcode
                kryo.getContext(), kryo.isRegistrationRequired(), kryo.getNextRegistrationId(),
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
         * Register a packet to the protocol.
         * @param packet The packet to register.
         */
        public Builder addPacket(@NonNull Class<?> packet) {
            if (protocol.kryo.getClassResolver().getRegistration(packet) != null) return this;

            try {
                protocol.kryo.register(packet);
                for (Field field : packet.getDeclaredFields()) addPacket(field.getType());
            } catch (InaccessibleObjectException ignore) {}
            return this;
        }

        /**
         * Enables compression on the protocol.
         * @param type The type of compression to enable. Defaults to NONE.
         */
        public Builder enableCompression(@NonNull CompressionType type) {
            protocol.compression = type;
            return this;
        }

        /**
         * Set the compression level if compression is enabled. Be sure to check the supported level for each protocol online.
         * @param level The compression level to set.
         */
        public Builder setCompressionLevel(int level) {
            protocol.compressionLevel = level;
            return this;
        }

        /**
         * Set the maximum packet size this protocol can handle. The higher the value, the more ram will be allocated when sending and receiving packets.
         * The maximum packet size defaults to 16kb
         * @param maxPacketSize The maximum size a packet can be in bytes
         */
        public Builder setMaxPacketSize(int maxPacketSize) {
            protocol.maxPacketSize = maxPacketSize;
            return this;
        }

        /**
         * Build the protocol.
         * @return A finished protocol that can be loaded using {@link ProtoWeaver#load(Protocol)}.
         */
        public Protocol build() {
            if (protocol.compression != CompressionType.NONE && protocol.compressionLevel == -37)
                protocol.compressionLevel = protocol.compression.getDefaultLevel();
            return protocol;
        }
    }
}