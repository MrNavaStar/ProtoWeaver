package me.mrnavastar.protoweaver.protocol.protomessage;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.util.BufUtils;

import java.util.Date;

@Getter
@NoArgsConstructor
public class Message implements ProtoPacket {

    private String channel;
    private String message;
    private Date time;

    public Message(String channel, String message) {
        this.channel = channel;
        this.message = message;
        this.time = new Date();
    }

    @Override
    public void encode(ByteBuf buf) {
        BufUtils.writeString(buf, channel);
        BufUtils.writeString(buf, message);
        buf.writeLong(time.getTime());
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        channel = BufUtils.readString(buf);
        message = BufUtils.readString(buf);
        time = new Date(buf.readLong());
    }
}