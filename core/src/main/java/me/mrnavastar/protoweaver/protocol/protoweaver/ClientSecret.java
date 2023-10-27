package me.mrnavastar.protoweaver.protocol.protoweaver;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.util.BufUtils;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClientSecret implements ProtoPacket {

    private String secret;

    @Override
    public void encode(ByteBuf buf) {
        BufUtils.writeString(buf, secret);
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        secret = BufUtils.readString(buf);
    }
}