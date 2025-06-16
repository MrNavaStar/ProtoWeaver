package me.mrnavastar.protoweaver.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class ProtoSerializer<T> {

    public abstract T read(ByteArrayInputStream buffer);

    public abstract void write(ByteArrayOutputStream buffer, T value);

}