package me.mrnavastar.protoweaver.api;

public class ProtoWeaverAPI {

    public static ProtoBuilder buildProtocol(String namespace, String name) {
        return new ProtoBuilder(namespace + ":" + name);
    }
}