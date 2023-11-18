package me.mrnavastar.protoweaver.api.util;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A small helper class for working with Netty's {@link ByteBuf}
 */
@UtilityClass
public class BufUtils {

    /**
     * Writes the given {@link String} to the given {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to be written to.
     * @param s The {@link String} to write.
     */
    public static void writeString(@NonNull ByteBuf buf, @NonNull String s) {
        buf.writeInt(s.length());
        buf.writeBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reads a {@link String} from the given {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to read from.
     * @return The {@link String} that was read.
     */
    public static String readString(@NonNull ByteBuf buf) {
        int len = buf.readInt();
        return buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }

    /**
     * Writes the given {@link UUID} to the given {@link ByteBuf}
     * @param buf The {@link ByteBuf} to be written to.
     * @param uuid The {@link UUID} to write.
     */
    public static void writeUUID(@NonNull ByteBuf buf, @NonNull UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Reads a {@link UUID} from the given {@link ByteBuf}.
     * @param buf The {@link ByteBuf} to read from.
     * @return The {@link UUID} that was read.
     */
    public static UUID readUUID(@NonNull ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }
}