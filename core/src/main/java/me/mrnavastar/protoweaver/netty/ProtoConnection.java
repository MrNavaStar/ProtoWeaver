package me.mrnavastar.protoweaver.netty;

import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.*;
import lombok.Getter;
import lombok.NonNull;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.protocol.CompressionType;
import me.mrnavastar.protoweaver.protocol.Protocol;
import me.mrnavastar.protoweaver.protocol.Side;

public class ProtoConnection {

    private final ProtoPacketSender packetSender;
    private final ProtoPacketDecoder packetDecoder;
    @Getter
    private final Side side;
    @Getter
    private final Channel channel;
    @Getter
    private Protocol protocol;

    public ProtoConnection(@NonNull Protocol protocol, @NonNull Side side, @NonNull ChannelPipeline pipeline) {
        this.side = side;
        this.packetSender = new ProtoPacketSender(this, side);
        this.packetDecoder = new ProtoPacketDecoder(this, side);
        this.channel = pipeline.channel();
        this.protocol = protocol;

        // Sender must be active first
        pipeline.addLast("protoSender", packetSender);
        pipeline.addLast("protoDecoder", packetDecoder);
        setCompression(protocol);
    }

    private void enableSSL() {

    }

    private void setCompression(@NonNull Protocol protocol) {
        CompressionType compression = this.protocol.getCompression();
        if (protocol.getCompression().equals(compression)) return;

        if (channel.pipeline().names().contains("compressionEncoder")) {
            channel.pipeline().remove("compressionEncoder");
            channel.pipeline().remove("compressionDecoder");
        }

        int level = protocol.getCompressionLevel();
        switch (protocol.getCompression()) {
            case BROTLI -> {
                channel.pipeline().addBefore("protoDecoder", "compressionEncoder", new BrotliEncoder(new Encoder.Parameters().setQuality(level)));
                channel.pipeline().addAfter("compressionEncoder", "compressionDecoder", new BrotliDecoder());
            }
            case GZIP -> {
                channel.pipeline().addBefore("protoDecoder", "compressionEncoder", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, level));
                channel.pipeline().addAfter("compressionEncoder", "compressionDecoder", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
            }
            case SNAPPY -> {
                channel.pipeline().addBefore("protoDecoder", "compressionEncoder", new SnappyFrameEncoder());
                channel.pipeline().addAfter("compressionEncoder", "compressionDecoder", new SnappyFrameDecoder());
            }
            case LZ4 -> {
                channel.pipeline().addBefore("protoDecoder", "compressionEncoder", new Lz4FrameEncoder(level > 0));
                channel.pipeline().addAfter("compressionEncoder", "compressionDecoder", new Lz4FrameDecoder());
            }
        }
    }

    public void upgradeProtocol(@NonNull Protocol protocol) {
        packetDecoder.setHandler(protocol.newHandler(side));
        setCompression(protocol);
        this.protocol = protocol;
    }

    public void send(@NonNull ProtoPacket packet) {
        packetSender.send(packet);
    }

    public void disconnect() {
        channel.close();
    }
}