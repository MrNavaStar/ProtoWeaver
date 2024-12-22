package me.mrnavastar.protoweaver.core.util;

import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoSerializer;
import me.mrnavastar.r.R;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.exception.InsecureException;
import org.apache.fury.logging.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ObjectSerializer {

    private final ThreadSafeFury fury = Fury.builder().withJdkClassSerializableCheck(false).buildThreadSafeFury();

    static {
        // Make fury be quiet
        LoggerFactory.disableLogging();
    }

    private void recursiveRegister(Class<?> type, List<Class<?>> registered) {
        if (type == null || type == Object.class || registered.contains(type)) return;
        fury.register(type);
        registered.add(type);

        List.of(type.getDeclaredFields()).forEach(field -> recursiveRegister(field.getType(), registered));
        List.of(R.of(type).generics()).forEach(t -> recursiveRegister(t, registered));
        if (!type.isEnum()) recursiveRegister(type.getSuperclass(), registered);
    }

    public void register(Class<?> type) {
        recursiveRegister(type, new ArrayList<>());
    }

    @SneakyThrows
    public void register(Class<?> type, Class<? extends ProtoSerializer> serializer) {
        try {
            serializer.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            serializer.getDeclaredConstructor(Class.class).newInstance(type);
        }
        fury.registerSerializer(type, ProtoSerializer.SerializerWrapper.class);
    }

    public byte[] serialize(Object object) throws IllegalArgumentException {
        try {
            return fury.serialize(object);
        } catch (InsecureException e) {
            throw new IllegalArgumentException("unregistered object: " + object.getClass().getName());
        }
    }

    public Object deserialize(byte[] bytes) throws IllegalArgumentException {
        try {
            return fury.deserialize(bytes);
        } catch (InsecureException e) {
            String packet = e.getMessage().split(" is not registered")[0].replace("class ", "");
            throw new IllegalArgumentException("unregistered object: " + packet);
        }
    }
}