package me.mrnavastar.protoweaver.api.protocol.protomessage;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;

@Getter
@EqualsAndHashCode
public class Message {

    private final String channel;
    private final String message;
    @EqualsAndHashCode.Exclude
    private final Date time;

    public Message(String channel, String message) {
        this.channel = channel;
        this.message = message;
        this.time = new Date();
    }
}