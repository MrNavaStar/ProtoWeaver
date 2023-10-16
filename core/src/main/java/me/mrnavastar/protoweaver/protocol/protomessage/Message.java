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
        channel = new String(buf.readBytes(channelLen).array(), StandardCharsets.UTF_8);
        int messageLen = buf.readInt();
        message = new String(buf.readBytes(messageLen).array(), StandardCharsets.UTF_8);
        time = new Date(buf.readLong());
    }
}