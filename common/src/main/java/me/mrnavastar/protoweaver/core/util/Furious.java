package me.mrnavastar.protoweaver.core.util;

import me.mrnavastar.r.R;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Furious {

    private static final ThreadSafeFury FURY = Fury.builder().withJdkClassSerializableCheck(false).buildThreadSafeFury();

    static {
        // Make fury be quiet
        LoggerFactory.disableLogging();
    }

    private static void recursiveRegister(Class<?> type, List<Class<?>> registered) {
        if (type == null || registered.contains(type)) return;
        FURY.register(type);
        registered.add(type);

        List.of(type.getDeclaredFields()).forEach(field -> recursiveRegister(field.getType(), registered));
        List.of(R.of(type).generics()).forEach(t -> recursiveRegister(t, registered));
        recursiveRegister(type.getSuperclass(), registered);
    }

    public static void register(Class<?> type) {
        recursiveRegister(type, new ArrayList<>());
    }

    public static byte[] serialize(Object object) {
        return FURY.serialize(object);
    }

    public static Object deserialize(byte[] bytes) {
        return FURY.deserialize(bytes);
    }
}
