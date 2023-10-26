package me.mrnavastar.protoweaver.netty;

import com.aayushatharva.brotli4j.encoder.Encoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.*;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.protocol.CompressionType;
import me.mrnavastar.protoweaver.protocol.Protocol;

public class ProtoConnection {

    private final ProtoPacketSender packetSender = new ProtoPacketSender(this);
    private final ProtoPacketDecoder packetDecoder = new ProtoPacketDecoder(this);
    @Getter
    private Protocol protocol;
    @Getter
    private final Channel channel;

    public ProtoConnection(Protocol protocol, ProtoPacketHandler handler, ChannelPipeline pipeline) {
        this.protocol = protocol;
        this.channel = pipeline.channel();
        packetDecoder.setHandler(handler);

        pipeline.addLast("protoDecoder", packetDecoder);
        pipeline.addLast("protoSender", packetSender);
        setCompression(protocol);
    }

    private void enabledSSL(Protocol protocol) {

    }

    private void setCompression(Protocol protocol) {
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

    public void upgradeProtocol(Protocol protocol, ProtoPacketHandler handler) {
        packetDecoder.setHandler(handler);
        setCompression(protocol);
        this.protocol = protocol;
    }

    public void send(ProtoPacket packet) {
        packetSender.send(packet);
    }

    public void disconnect() {
        channel.close();
    }
}