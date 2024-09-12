package me.mrnavastar.protoweaver.proxy.api;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

import java.net.SocketAddress;
import java.util.Objects;

@Getter
public class ProtoServer {

    private final String name;
    private final SocketAddress address;
    private ProtoConnection connection;

    public ProtoServer(String name, SocketAddress address) {
        this.name = name;
        this.address = address;
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProtoServer server && Objects.equals(server.name, name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name + " : " + address;
    }
}
