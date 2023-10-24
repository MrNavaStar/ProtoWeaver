package me.mrnavastar.protoweaver.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.BrotliDecoder;
import io.netty.handler.codec.compression.BrotliEncoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
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
    private final Channel channel;
    @Getter
    private CompressionType compression;

    public ProtoConnection(Protocol protocol, ProtoPacketHandler handler, ChannelPipeline pipeline) {
        this.protocol = protocol;
        this.compression = protocol.getCompression();
        this.channel = pipeline.channel();
        packetDecoder.setHandler(handler);

        pipeline.addLast("protoDecoder", packetDecoder);
        pipeline.addLast("protoSender", packetSender);
        setCompression(protocol);

        System.out.println(pipeline.names());
    }

    private void setCompression(Protocol protocol) {
        if (protocol.getCompression().equals(compression)) return;

        if (!compression.equals(CompressionType.NONE)) {
            channel.pipeline().remove("compressionEncoder");
            channel.pipeline().remove("compressionDecoder");
        }

        switch (protocol.getCompression()) {
            case BROTLI -> {
                channel.pipeline().addBefore("protoDecoder", "compressionEncoder", new BrotliEncoder());
                channel.pipeline().addAfter("compressionEncoder", "compressionDecoder", new BrotliDecoder());
            }
            case GZIP -> {
                channel.pipeline().addFirst("compressionDecoder", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
                channel.pipeline().addFirst("compressionEncoder", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
            }
        }

        compression = protocol.getCompression();
        System.out.println(channel.pipeline().names());
    }

    public void upgradeProtocol(Protocol protocol, ProtoPacketHandler handler) {
        this.protocol = protocol;
        packetDecoder.setHandler(handler);
        setCompression(protocol);
    }

    public void send(ProtoPacket packet) {
        packetSender.send(packet);
    }

    public void disconnect() {
        channel.close();
    }
}