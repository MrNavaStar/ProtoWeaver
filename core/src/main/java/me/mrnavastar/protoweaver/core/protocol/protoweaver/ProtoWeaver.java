package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.Getter;
import me.mrnavastar.protoweaver.api.ProtoBuilder;
import me.mrnavastar.protoweaver.api.Sender;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Protocol;

import me.mrnavastar.protoweaver.core.util.ProtoLogger;

public class ProtoWeaver extends me.mrnavastar.protoweaver.api.ProtoWeaver {

    @Getter
    protected static final Protocol protocol = ProtoBuilder.protocol("protoweaver", "internal")
            .setServerHandler(ServerHandler.class)
            .setClientHandler(ClientHandler.class)
            .addPacket(ClientSecret.class)
            .addPacket(AuthStatus.class)
            .addPacket(ProtocolStatus.class)
            .build();

    static {
        load(protocol);
    }

    protected void disconnectIfNeverUpgraded(ProtoConnection connection, Sender sender) {
        if (!connection.getProtocol().getName().equals(protocol.getName())) return;
        if (sender != null) {
            sender.disconnect();
            return;
        }
        connection.disconnect();
    }

    protected void disconnectIfNeverUpgraded(ProtoConnection connection) {
        disconnectIfNeverUpgraded(connection, null);
    }

    protected void protocolNotLoaded(ProtoConnection connection, String name) {
        ProtoLogger.warn("Protocol: " + name + " is not loaded! Closing connection!");
        Sender sender = connection.send(new ProtocolStatus(connection.getProtocol().getName(), name, ProtocolStatus.Status.MISSING));
        disconnectIfNeverUpgraded(connection, sender);
    }
}