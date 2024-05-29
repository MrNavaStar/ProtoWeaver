package me.mrnavastar.protoweaver.api.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.netty.ProtoPacketHandler;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This provider represents a connection to either a client or a server
 */
public class ProtoConnection {

    private static final ConcurrentHashMap<String, Integer> connectionCount = new ConcurrentHashMap<>();

    private final ProtoPacketHandler packetHandler;
    private final Channel channel;
    private final ChannelPipeline pipeline;
    @Getter
    private ProtoConnectionHandler handler;

    /**
     * Get the connections current protocol.
     */
    @Getter
    private Protocol protocol;

    /**
     * Get the side that this connection is on. Always returns {@link Side#CLIENT} on client and {@link Side#SERVER} on server.
     */
    @Getter
    private final Side side;

    /**
     * Get which side closed the connection.
     */
    @Getter
    private Side disconnecter;

    public ProtoConnection(@NonNull Protocol protocol, @NonNull Side side, @NonNull Channel channel) {
        this.side = side;
        this.disconnecter = Side.SERVER == side ? Side.CLIENT : Side.SERVER;
        this.protocol = protocol;
        this.handler = createHandler(protocol, side);
        this.packetHandler = new ProtoPacketHandler(this, connectionCount);
        packetHandler.setHandler(handler);
        this.channel = channel;
        this.pipeline = channel.pipeline();

        pipeline.addLast("packetHandler", packetHandler);
        setCompression(protocol);
    }

    @SneakyThrows
    private static ProtoConnectionHandler createHandler(@NonNull Protocol protocol, @NonNull Side side) {
        return switch (side) {
            case CLIENT -> {
                if (protocol.getClientHandler() == null) throw new RuntimeException("Failed to create client handler for protocol: " + protocol);
                yield protocol.getClientHandler().getDeclaredConstructor().newInstance();
            }
            case SERVER -> {
                if (protocol.getServerHandler() == null) throw new RuntimeException("Failed to create server handler for protocol: " + protocol);
                yield protocol.getServerHandler().getDeclaredConstructor().newInstance();
            }
        };
    }

    private void setCompression(@NonNull Protocol protocol) {
        CompressionType compression = this.protocol.getCompression();
        if (protocol.getCompression().equals(compression)) return;

        if (pipeline.names().contains("compressionEncoder")) {
            pipeline.remove("compressionEncoder");
            pipeline.remove("compressionDecoder");
        }

        int level = protocol.getCompressionLevel();
        switch (protocol.getCompression()) {
            case GZIP -> {
                pipeline.addBefore("packetHandler", "compressionEncoder", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, level));
                pipeline.addAfter("compressionEncoder", "compressionDecoder", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
            }
            case SNAPPY -> {
                pipeline.addBefore("packetHandler", "compressionEncoder", new SnappyFrameEncoder());
                pipeline.addAfter("compressionEncoder", "compressionDecoder", new SnappyFrameDecoder());
            }
            case FAST_LZ -> {
                pipeline.addBefore("packetHandler", "compressionEncoder", new FastLzFrameEncoder(level));
                pipeline.addAfter("compressionEncoder", "compressionDecoder", new FastLzFrameDecoder());
            }
        }
    }

    /**
     * Changes the current connection protocol to the given protocol.
     * NOTE: You must call this on both the client and server or else you will have a protocol mismatch.
     * @param protocol The protocol the connection will switch to.
     */
    public void upgradeProtocol(@NonNull Protocol protocol) {
        setCompression(protocol);

        try {
            this.handler = createHandler(protocol, side);
        } catch (Exception e) {
            ProtoLogger.error("Failed to start protocol: " + protocol);

            Class<?> handler = side.equals(Side.SERVER) ? protocol.getServerHandler() : protocol.getClientHandler();
            if (Modifier.isAbstract(handler.getModifiers())) {
                ProtoLogger.error(protocol + "'s connection handler is an abstract class, which is not allowed!");
                ProtoLogger.error(protocol + "'s mod author must address this issue");
            } else {
                ProtoLogger.error(protocol + "'s connection handler doesn't have a zero arg constructor");
                ProtoLogger.error(protocol + "'s mod author must add one to: " + handler.getName());
            }

            disconnect();
        }

        connectionCount.put(protocol.toString(), connectionCount.getOrDefault(protocol.toString(), 1) - 1);
        this.protocol = protocol;
        connectionCount.put(protocol.toString(), connectionCount.getOrDefault(protocol.toString(), 0) + 1);
        packetHandler.setHandler(handler);

        try {
            this.handler.onReady(this);
        } catch (Exception e) {
            ProtoLogger.error("Protocol: " + protocol + " threw an error on initialization!");
            e.printStackTrace();
        }
    }

    /**
     * @return The number of connected clients the passed in protocol is currently serving.
     */
    public static int getConnectionCount(Protocol protocol) {
        return connectionCount.getOrDefault(protocol.toString(), 0);
    }

    /**
     * Checks if the connection is open.
     * @return True if open, false if closed.
     */
    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * Get the remote address of the connection. Check {@link InetSocketAddress} for more information.
     * @return {@link InetSocketAddress}
     */
    public InetSocketAddress getRemoteAddress() {
        return ((InetSocketAddress) channel.remoteAddress());
    }

    /**
     * Sends a {@link Object} to the connected peer.
     * @return A {@link Sender} that can be used to close the connection after the packet is sent.
     */
    public Sender send(@NonNull Object packet) {
        return packetHandler.send(packet);
    }

    /**
     * Closes the connection if it is open. Calling this function on a closed connection does nothing.
     */
    public void disconnect() {
        if (isOpen()) channel.close();
        disconnecter = side;
    }
}