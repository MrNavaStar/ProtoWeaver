package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.util.BufUtils;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ClientSecret implements ProtoPacket {

    private String secret;

    @Override
    public void encode(ByteBuf buf) {
        BufUtils.writeString(buf, secret);
    }

    @Override
    public void decode(ByteBuf buf) {
        secret = BufUtils.readString(buf);
    }
}