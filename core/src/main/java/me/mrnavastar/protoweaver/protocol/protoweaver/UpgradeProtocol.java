package me.mrnavastar.protoweaver.protocol.protoweaver;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.util.BufUtils;

@Getter
public class UpgradeProtocol implements ProtoPacket {

    private String protocol;

    public UpgradeProtocol() {}

    public UpgradeProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public void encode(ByteBuf buf) {
        BufUtils.writeString(buf, protocol);
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        protocol = BufUtils.readString(buf);
    }
}
