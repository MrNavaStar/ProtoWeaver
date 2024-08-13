package me.mrnavastar.protoweaver.proxy.api;

import lombok.Getter;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

import java.net.SocketAddress;

@Setter
@Getter
public class ProtoServer {

    private final String name;
    private final SocketAddress address;
    private ProtoConnection connection;

    public ProtoServer(String name, SocketAddress address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public String toString() {
        return name + " : " + address;
    }
}
