package me.mrnavastar.protoweaver.api.protocol;

import lombok.*;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.auth.AuthProvider;
import me.mrnavastar.protoweaver.api.auth.Authenticator;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.core.netty.ProtoChannelHandler;
import me.mrnavastar.protoweaver.core.util.ObjectSerializer;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

/**
 * Stores all the registered packets, settings and additional configuration of a {@link ProtoWeaver} protocol.
 */
@EqualsAndHashCode
public class Protocol {

    @EqualsAndHashCode.Exclude
    private final ObjectSerializer serializer = new ObjectSerializer();
    @EqualsAndHashCode.Exclude
    private final MessageDigest packetMD = MessageDigest.getInstance("SHA-1");

    @Getter private final String namespace;
    @Getter private final String name;
    @Getter private CompressionType compression = CompressionType.NONE;
    @Getter private int compressionLevel = -37;
    @Getter private boolean async = false;
    @Getter private int maxPacketSize = 16384;
    @Getter private int maxConnections = -1;
    @Getter private Level loggingLevel = Level.ALL;
    @Getter private Side polarity = Side.CLIENT;

    @EqualsAndHashCode.Exclude private Class<? extends ProtoConnectionHandler> handler;
    @EqualsAndHashCode.Exclude private Class<? extends Authenticator> authenticator;
    @EqualsAndHashCode.Exclude private Class<? extends AuthProvider> authProvider;

    private Protocol(String namespace, String name) throws NoSuchAlgorithmException {
        this.namespace = namespace;
        this.name = name;
    }

    /**
     * <p>Creates a new protocol builder. A good rule of thumb for naming that ensures maximum compatibility is to use
     * your mod id or project id for the namespace and to give the name something unique.</p>
     * <br>For example: "protoweaver:proto-message"</br>
     * @param namespace Usually should be set to your mod id or project id
     * @param name The name of your protocol.
     */
    @SneakyThrows
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

    @SneakyThrows
    public ProtoConnectionHandler newConnectionHandler() {
        if (handler == null) throw new RuntimeException("No client connection handler set for protocol: " + this);
        return handler.getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    public Authenticator newAuthenticator() {
        if (authenticator == null) throw new RuntimeException("No authenticator set for protocol: " + this);
        return authenticator.getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    public AuthProvider newAuthProvider() {
        if (authProvider == null) throw new RuntimeException("No auth provider set for protocol: " + this);
        return authProvider.getDeclaredConstructor().newInstance();
    }

    public byte[] serialize(@NonNull Object packet) throws IllegalArgumentException {
        return serializer.serialize(packet);
    }

    public Object deserialize(byte @NonNull [] packet) throws IllegalArgumentException {
        return serializer.deserialize(packet);
    }

    @SneakyThrows
    public byte[] getSHA1() {
        MessageDigest md = (MessageDigest) this.packetMD.clone();
        md.update(toString().getBytes(StandardCharsets.UTF_8));
        md.update(ByteBuffer.allocate(12)
                .putInt(compressionLevel)
                .putInt(compression.ordinal())
                .putInt(maxPacketSize)
                .array());
        return md.digest();
    }

    /**
     * @return The number of connected clients this protocol is currently serving.
     */
    public int getConnections() {
        return ProtoChannelHandler.getConnectionCount(this);
    }

    /**
     * Determine if a side requires auth by checking to see if an auth handler was set for the given side.
     * @param side The {@link Side} to check for an auth handler.
     */
    public boolean requiresAuth(@NonNull Side side) {
        if (side.equals(Side.CLIENT)) return authProvider != null;
        return authenticator != null;
    }

    public void logInfo(@NonNull String message) {
        if (loggingLevel.intValue() <= Level.INFO.intValue()) ProtoLogger.info("[" + this + "]: " + message);
    }

    public void logWarn(@NonNull String message) {
        if (loggingLevel.intValue() <= Level.WARNING.intValue()) ProtoLogger.warn("[" + this + "]: " + message);
    }

    public void logErr(@NonNull String message) {
        if (loggingLevel.intValue() <= Level.SEVERE.intValue()) ProtoLogger.err("[" + this + "]: " + message);
    }

    @Override
    public String toString() {
        return namespace + ":" + name;
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private final Protocol protocol;

        /**
         * Set the packet handler that the {@link Protocol} will use to process inbound packets.
         * <br><br>
         * NOTE: You will likely need to set the handler different on each side of the protocol (Client/Server).
         * You can use {@link Protocol#modify()} to edit a {@link Protocol} after it has been built.
         * @param handler The class of the packet handler.
         */
        @SneakyThrows
        public Builder setHandler(Class<? extends ProtoConnectionHandler> handler) {
            if (Modifier.isAbstract(handler.getModifiers())) throw new IllegalArgumentException("Handler class cannot be abstract: " + handler);
            if (handler.getDeclaredConstructor().getParameterCount() != 0) throw new IllegalArgumentException("Handler class must have a zero arg constructor: " + handler);
            protocol.handler = handler;
            return this;
        }

        /**
         * Set the auth handler that the server will use to process inbound client secrets.
         * @param authenticator The class of the authenticator.
         */
        @SneakyThrows
        public Builder setAuthenticator(Class<? extends Authenticator> authenticator) {
            if (Modifier.isAbstract(authenticator.getModifiers())) throw new IllegalArgumentException("Handler class: " + authenticator + " cannot be abstract");
            if (authenticator.getDeclaredConstructor().getParameterCount() != 0) throw new IllegalArgumentException("Handler class: " + authenticator + " must have a zero arg constructor");
            protocol.authenticator = authenticator;
            return this;
        }

        /**
         * Set the auth handler that the client will use to get the secret that will be sent to the server.
         * @param authProvider The class of the authProvider.
         */
        @SneakyThrows
        public Builder setAuthProvider(Class<? extends AuthProvider> authProvider) {
            if (Modifier.isAbstract(authProvider.getModifiers())) throw new IllegalArgumentException("Handler class: " + authProvider + " cannot be abstract");
            if (authProvider.getDeclaredConstructor().getParameterCount() != 0) throw new IllegalArgumentException("Handler class: " + authProvider + " cannot be abstract");
            protocol.authProvider = authProvider;
            return this;
        }

        /**
         * Register a class to the {@link Protocol}. Does nothing if the class has already been registered.
         * @param packet The packet to register.
         */
        public Builder addPacket(@NonNull Class<?> packet) {
            protocol.serializer.register(packet);
            protocol.packetMD.update(packet.getName().getBytes(StandardCharsets.UTF_8));
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
         * Make this {@link Protocol} handle packets asynchronously, instead of one after the other. This means that the
         * {@link ProtoConnectionHandler#handlePacket(ProtoConnection, Object)} method may be called before the previous
         * call is finished. This will improve performance if your protocol is designed with this in mind.
         */
        public Builder setAsync() {
            protocol.async = true;
            return this;
        }

        /**
         * Set the maximum packet size this {@link Protocol} can handle. The higher the value, the more ram will be
         * allocated when sending and receiving packets. The maximum packet size defaults to 16kb.
         * @param maxPacketSize The maximum size a packet can be in bytes.
         */
        public Builder setMaxPacketSize(int maxPacketSize) {
            protocol.maxPacketSize = maxPacketSize;
            return this;
        }

        /**
         * Set the number of maximum concurrent connections this {@link Protocol} will allow. Any connections over this limit
         * will be disconnected. The maximum connections defaults to -1 and allows any number of connections.
         * @param maxConnections The maximum concurrent connections.
         */
        public Builder setMaxConnections(int maxConnections) {
            protocol.maxConnections = maxConnections;
            return this;
        }

        /**
         * Sets the logging level for this {@link Protocol}.
         */
        public Builder setLoggingLevel(Level level) {
            protocol.loggingLevel = level;
            return this;
        }

        /**
         * On velocity, this will set the direction this protocol serves. {@link Side#SERVER} will make it serve backend
         * servers, while {@link Side#CLIENT} will make it serve in front of the proxy. {@link Side#BOTH} will serve both.
         * <br></br>
         * NOTE: ProtoClients do not care about polarity, and will attempt to use the protocol regardless of what it is
         * set to.
         * @param side The side to serve this {@link Protocol} to.
         */
        public Builder setPolarity(Side side) {
            protocol.polarity = side;
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

        /**
         * Equivalent to calling {@link Builder#build()} and {@link ProtoWeaver#load(Protocol)}.
         * @return The {@link Protocol} that was built and loaded.
         */
        public Protocol load() {
            ProtoWeaver.load(build());
            return protocol;
        }

        @Override
        public String toString() {
            return protocol.toString();
        }
    }
}