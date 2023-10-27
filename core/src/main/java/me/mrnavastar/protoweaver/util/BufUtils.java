package me.mrnavastar.protoweaver.util;

import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class BufUtils {

    public static void writeString(ByteBuf buf, String s) {
        buf.writeInt(s.length());
        buf.writeBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String readString(ByteBuf buf) {
        int len = buf.readInt();
        return buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }
}