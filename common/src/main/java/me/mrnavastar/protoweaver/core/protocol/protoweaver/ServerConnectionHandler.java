package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

import java.util.concurrent.ConcurrentHashMap;

public class ServerConnectionHandler extends InternalConnectionHandler implements ProtoConnectionHandler {

    private static final ConcurrentHashMap<String, Integer> connectionCount = new ConcurrentHashMap<>();

    private boolean authenticated = false;
    private Protocol nextProtocol = null;
    private ServerAuthHandler authHandler = null;

    @Override
    public void onDisconnect(ProtoConnection connection) {
        if (connection.getProtocol().equals(protocol)) return;
        // Decrement the connection count of the current protocol
        connectionCount.put(connection.getProtocol().toString(), connectionCount.getOrDefault(connection.getProtocol().toString(), 1) - 1);
    }

    @SneakyThrows
    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case START -> {
                    // Check if protocol loaded
                    nextProtocol = ProtoWeaver.getLoadedProtocol(status.getNextProtocol());
                    if (nextProtocol == null) {
                        protocolNotLoaded(connection, status.getNextProtocol());
                        return;
                    }

                    if (connectionCount.getOrDefault(nextProtocol.toString(), 0) >= nextProtocol.getMaxConnections()) {
                        status.setStatus(ProtocolStatus.Status.FULL);
                        disconnectIfNeverUpgraded(connection, connection.send(status));
                        return;
                    }

                    if (nextProtocol.hashCode() != status.getNextProtocolHash()) {
                        ProtoLogger.error("Protocol: \"" + nextProtocol + "\" has a mismatch with the version on the client!");
                        ProtoLogger.error("Double check that all packets are registered in the same order and all settings are the same.");
                        status.setStatus(ProtocolStatus.Status.MISMATCH);
                        disconnectIfNeverUpgraded(connection, connection.send(status));
                        return;
                    }

                    // Check if protocol needs authentication
                    if (nextProtocol.getServerAuthHandler() != null) {
                        authHandler = nextProtocol.getServerAuthHandler().getDeclaredConstructor().newInstance();
                        connection.send(AuthStatus.REQUIRED);
                        return;
                    }

                    authenticated = true;
                }
                case MISSING -> {
                    ProtoLogger.error("Protocol: \"" + status.getNextProtocol() + "\" is not loaded on client!");
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }

        // Authenticate client
        if (nextProtocol != null && packet instanceof String secret) {
            authenticated = authHandler.handleAuth(connection, secret);
        }

        if (!authenticated) {
            Sender sender = connection.send(AuthStatus.DENIED);
            disconnectIfNeverUpgraded(connection, sender);
            return;
        }

        // Upgrade protocol
        connection.send(AuthStatus.OK);
        connection.send(new ProtocolStatus(connection.getProtocol().toString(), nextProtocol.toString(), 0, ProtocolStatus.Status.UPGRADE));
        connection.upgradeProtocol(nextProtocol);
        // Increment the connection count of the new protocol
        connectionCount.put(nextProtocol.toString(), connectionCount.getOrDefault(nextProtocol.toString(), 0) + 1);
    }
}