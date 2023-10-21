package me.mrnavastar.protoweaver.protocol.protomessage;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoPacket;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Getter
public class Message extends ProtoPacket {

    private String channel;
    private String message;
    private Date time;

    public Message() {}

    public Message(String channel, String message) {
        this.channel = channel;
        this.message = message;
        this.time = new Date();
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(channel.length());
        buf.writeBytes(channel.getBytes(StandardCharsets.UTF_8));
        buf.writeInt(message.length());
        buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
        buf.writeLong(time.getTime());
    }

    @Override
    public void decode(ByteBuf buf) throws IndexOutOfBoundsException {
        int channelLen = buf.readInt();
        channel = buf.readCharSequence(channelLen, StandardCharsets.UTF_8).toString();
        int messageLen = buf.readInt();
        message = buf.readCharSequence(messageLen, StandardCharsets.UTF_8).toString();
        time = new Date(buf.readLong());
    }
}