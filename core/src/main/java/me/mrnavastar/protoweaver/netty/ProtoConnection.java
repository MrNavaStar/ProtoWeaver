package me.mrnavastar.protoweaver.netty;

import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.protocol.Protocol;

@Getter
public class ProtoConnection {

    private final ProtoPacketSender packetSender = new ProtoPacketSender();
    private final ProtoPacketDecoder packetDecoder = new ProtoPacketDecoder(this);
    private Protocol protocol;
    private ProtoPacketHandler handler;

    public ProtoConnection(Protocol protocol, ProtoPacketHandler handler, ChannelPipeline pipeline) {
        this.protocol = protocol;
        this.handler = handler;
        pipeline.addLast("protoDecoder", packetDecoder);
        pipeline.addLast("protoSender", packetSender);
    }

    public void upgradeProtocol(Protocol newProtocol, ProtoPacketHandler handler) {
        this.protocol = newProtocol;
        this.handler = handler;
        packetDecoder.setHandler(handler);
    }

    public void send(ProtoPacket packet) {
        packetSender.send(packet);
    }

    public void disconnect() {

    }
}
