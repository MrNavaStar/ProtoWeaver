package me.mrnavastar.protoweaver.protocol.protoweaver;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.util.BufUtils;

@Getter
public class ClientAuthResponse implements ProtoPacket {

    private String secret;

    public ClientAuthResponse() {}

    public ClientAuthResponse(String secret) {
        this.secret = secret;
    }

    @Override
    public void encode(ByteBuf buf) {
        BufUtils.writeString(buf, secret);
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        secret = BufUtils.readString(buf);
    }
}
