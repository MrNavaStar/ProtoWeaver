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

import java.util.concurrent.ConcurrentLinkedQueue;

public class ProtoConnection {

    private final ProtoPacketSender packetSender = new ProtoPacketSender(this);
    private final ProtoPacketDecoder packetDecoder = new ProtoPacketDecoder(this);
    private final ConcurrentLinkedQueue<ProtoPacket> sendQue = new ConcurrentLinkedQueue<>();
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

    private void setCompression(Protocol protocol) {
        CompressionType compression = this.protocol.getCompression();
        if (protocol.getCompression().equals(compression)) return;

        if (!compression.equals(CompressionType.NONE)) {
            channel.pipeline().remove("compressionEncoder");
            channel.pipeline().remove("compressionDecoder");
        }

        int level = protocol.getCompressionLevel();
        if (level == -2) level = compression.getDefaultLevel();
        switch (compression) {
            case BROTLI -> {
                channel.pipeline().addBefore("protoDecoder", "compressionEncoder", new BrotliEncoder(new Encoder.Parameters().setQuality(level)));
                channel.pipeline().addAfter("compressionEncoder", "compressionDecoder", new BrotliDecoder());
            }
            case GZIP -> {
                channel.pipeline().addBefore("protoDecoder", "compressionEncoder", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, level));
                channel.pipeline().addAfter("compressionEncoder", "compressionDecoder", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
            }
            case ZSTD -> {

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