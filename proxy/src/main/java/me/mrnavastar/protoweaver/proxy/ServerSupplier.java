package me.mrnavastar.protoweaver.proxy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public interface ServerSupplier {

    @Getter
    @RequiredArgsConstructor
    class ServerInfo {
        private final String name;
        private final InetSocketAddress address;
    }

    ArrayList<ServerInfo> getServers();
}