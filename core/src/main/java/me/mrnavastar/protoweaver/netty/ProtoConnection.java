package me.mrnavastar.protoweaver.netty;

import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.protocol.Protocol;

public class ProtoConnection {

    private final ProtoPacketSender packetSender = new ProtoPacketSender(this);
    private final ProtoPacketDecoder packetDecoder = new ProtoPacketDecoder(this);
    @Getter
    private Protocol protocol;

    public ProtoConnection(Protocol protocol, ProtoPacketHandler handler, ChannelPipeline pipeline) {
        this.protocol = protocol;
        packetDecoder.setHandler(handler);
        pipeline.addLast("protoDecoder", packetDecoder);
        pipeline.addLast("protoSender", packetSender);
    }

    public void upgradeProtocol(Protocol newProtocol, ProtoPacketHandler handler) {
        this.protocol = newProtocol;
        packetDecoder.setHandler(handler);
    }

    public void send(ProtoPacket packet) {
        packetSender.send(packet);
    }

    public void disconnect() {

    }
}
