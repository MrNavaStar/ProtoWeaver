package me.mrnavastar.protoweaver.core.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.*;
import lombok.Getter;
import lombok.NonNull;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.api.Sender;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.protocol.protoweaver.ProtocolStatus;

import java.net.InetSocketAddress;

public class ProtoConnection implements me.mrnavastar.protoweaver.api.netty.ProtoConnection {

    private final ProtoPacketSender packetSender;
    private final ProtoPacketDecoder packetDecoder;
    @Getter
    private final Side side;
    private final Channel channel;
    private final ChannelPipeline pipeline;
    @Getter
    private Protocol protocol;
    @Getter
    private ProtoPacketHandler handler;

    public ProtoConnection(@NonNull Protocol protocol, @NonNull Side side, @NonNull Channel channel) {
        this.side = side;
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
            case LZ4 -> {
                pipeline.addBefore("protoDecoder", "compressionEncoder", new Lz4FrameEncoder(level > 0));
                pipeline.addAfter("compressionEncoder", "compressionDecoder", new Lz4FrameDecoder());
            }
        }
    }

    public void upgradeProtocol(@NonNull Protocol protocol) {
        setCompression(protocol);
        this.protocol = protocol;
        this.handler = protocol.newHandler(side);
        packetDecoder.setHandler(handler);
        this.handler.onReady(this);
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    public InetSocketAddress getRemoteAddress() {
        return ((InetSocketAddress) channel.remoteAddress());
    }

    public Sender send(@NonNull ProtoPacket packet) {
        return packetSender.send(packet);
    }

    public void disconnect() {
        if (isOpen()) channel.close();
    }
}