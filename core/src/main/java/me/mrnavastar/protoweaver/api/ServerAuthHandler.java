package me.mrnavastar.protoweaver.api;

import me.mrnavastar.protoweaver.netty.ProtoConnection;

public interface ServerAuthHandler {

    boolean handleAuth(ProtoConnection connection, String key);
}
