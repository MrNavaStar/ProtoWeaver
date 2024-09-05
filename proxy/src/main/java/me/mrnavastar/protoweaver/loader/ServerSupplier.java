package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.proxy.api.ProtoServer;

import java.util.List;

public interface ServerSupplier {

    List<ProtoServer> getServers();
}