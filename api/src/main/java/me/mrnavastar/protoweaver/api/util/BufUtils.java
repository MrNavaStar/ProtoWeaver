package me.mrnavastar.protoweaver.api.util;

import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

@UtilityClass
public class BufUtils {

    /**
     * Writes the given {@link String} to the given {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to be written to.
     * @param s The {@link String} to write.
     */
    public static void writeString(ByteBuf buf, String s) {
        buf.writeInt(s.length());
        buf.writeBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reads a {@link String} from the given {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to read from.
     * @return The {@link String} that was read.
     */
    public static String readString(ByteBuf buf) {
        int len = buf.readInt();
        return buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }
}