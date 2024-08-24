package me.mrnavastar.protoweaver.core.util;

import me.mrnavastar.r.R;
import org.apache.fury.BaseFury;
import org.apache.fury.exception.InsecureException;
import org.apache.fury.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Furious {

    static {
        // Make fury be quiet
        LoggerFactory.disableLogging();
    }

    private static void recursiveRegister(BaseFury fury, Class<?> type, List<Class<?>> registered) {
        if (type == null || type == Object.class || registered.contains(type)) return;
        fury.register(type);
        registered.add(type);

        List.of(type.getDeclaredFields()).forEach(field -> recursiveRegister(fury, field.getType(), registered));
        List.of(R.of(type).generics()).forEach(t -> recursiveRegister(fury, t, registered));
        if (!type.isEnum()) recursiveRegister(fury, type.getSuperclass(), registered);
    }

    public static void register(BaseFury fury, Class<?> type) {
        recursiveRegister(fury, type, new ArrayList<>());
    }

    public static byte[] serialize(BaseFury fury, Object object) throws InsecureException {
        try {
            return fury.serialize(object);
        } catch (InsecureException e) {
            throw new InsecureException("unregistered packet: " + object.getClass().getName());
        }
    }

    public static Object deserialize(BaseFury fury, byte[] bytes) throws InsecureException {
        try {
            return fury.deserialize(bytes);
        } catch (InsecureException e) {
            String packet = e.getMessage().split(" is not registered")[0].replace("class ", "");
            throw new InsecureException("unregistered packet: " + packet);
        }
    }
}