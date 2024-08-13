package me.mrnavastar.protoweaver.proxy;

import me.mrnavastar.protoweaver.proxy.api.ProtoServer;

import java.net.SocketAddress;
import java.util.List;

public interface ServerSupplier {

    List<ProtoServer> getServers();
}