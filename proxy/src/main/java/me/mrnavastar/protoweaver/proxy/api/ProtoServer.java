package me.mrnavastar.protoweaver.proxy.api;

import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

public record ProtoServer(String name, ProtoConnection connection) {}