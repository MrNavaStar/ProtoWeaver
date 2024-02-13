package me.mrnavastar.protoweaver.api.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.netty.ProtoPacketDecoder;
import me.mrnavastar.protoweaver.core.netty.ProtoPacketSender;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.net.InetSocketAddress;

/**
 * This provider represents a connection to either a client or a server
 */
public class ProtoConnection {

    private final ProtoPacketSender packetSender;
    private final ProtoPacketDecoder packetDecoder;
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

    public ProtoConnection(@NonNull Protocol protocol, @NonNull Side side, @NonNull Channel channel) throws NoSuchMethodException {
        this.side = side;
        this.disconnecter = Side.SERVER == side ? Side.CLIENT : Side.SERVER;
        this.protocol = protocol;
        this.handler = protocol.newHandler(side);
        this.packetSender = new ProtoPacketSender(this);
        this.packetDecoder = new ProtoPacketDecoder(this);
        packetDecoder.setHandler(handler);
        this.channel = channel;
        this.pipeline = channel.pipeline();

        pipeline.addLast("protoDecoder", packetDecoder);
        pipeline.addLast("protoSender", packetSender);
        setCompression(protocol);
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
                pipeline.addBefore("protoDecoder", "compressionEncoder", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, level));
                pipeline.addAfter("compressionEncoder", "compressionDecoder", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
            }
            case SNAPPY -> {
                pipeline.addBefore("protoDecoder", "compressionEncoder", new SnappyFrameEncoder());
                pipeline.addAfter("compressionEncoder", "compressionDecoder", new SnappyFrameDecoder());
            }
            case FAST_LZ -> {
                pipeline.addBefore("protoDecoder", "compressionEncoder", new FastLzFrameEncoder(level));
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
        this.protocol = protocol;

        try {
            this.handler = protocol.newHandler(side);
        } catch (NoSuchMethodException e) {
            ProtoLogger.error("Failed to upgrade to protocol: " + protocol.getName());
            Class<?> handler = side.equals(Side.CLIENT) ? protocol.getClientHandler() : protocol.getServerHandler();
            ProtoLogger.error(protocol.getName() + "'s connection handler doesn't have a zero arg constructor.");
            ProtoLogger.error("The mod author must add one to: " + handler.getName());
            disconnect();
        }

        packetDecoder.setHandler(handler);

        try {
            this.handler.onReady(this);
        } catch (Exception e) {
            ProtoLogger.error("Protocol: " + protocol.getName() + " threw an error on initialization!");
            e.printStackTrace();
        }
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
     * Sends a {@link ProtoPacket} to the connected peer.
     * @return A {@link Sender} that can be used to close the connection after the packet is sent.
     */
    @SneakyThrows
    public Sender send(@NonNull ProtoPacket packet) {
        return packetSender.send(packet);
    }

    /**
     * Closes the connection if it is open. Calling this function on a closed connection does nothing.
     */
    public void disconnect() {
        if (isOpen()) channel.close();
        disconnecter = side;
    }
}