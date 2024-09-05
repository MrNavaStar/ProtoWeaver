package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import lombok.SneakyThrows;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;

public class ServerConnectionHandler extends InternalConnectionHandler implements ProtoConnectionHandler {

    private boolean authenticated = false;
    private Protocol nextProtocol = null;
    private ServerAuthHandler authHandler = null;

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

                    if (nextProtocol.getPolarity().equals(Side.SERVER)) {
                        nextProtocol.logWarn("This protocol's polarity is set to server only.");
                        disconnectIfNeverUpgraded(connection);
                        return;
                    }

                    if (nextProtocol.getMaxConnections() != -1 && nextProtocol.getConnections() >= nextProtocol.getMaxConnections()) {
                        status.setStatus(ProtocolStatus.Status.FULL);
                        disconnectIfNeverUpgraded(connection, connection.send(status));
                        return;
                    }

                    if (nextProtocol.hashCode() != status.getNextProtocolHash()) {
                        nextProtocol.logErr("Mismatch with protocol version on the client!");
                        nextProtocol.logErr("Double check that all packets are registered in the same order and all settings are the same.");

                        status.setStatus(ProtocolStatus.Status.MISMATCH);
                        disconnectIfNeverUpgraded(connection, connection.send(status));
                        return;
                    }

                    if (nextProtocol.requiresAuth(Side.SERVER)) {
                        authHandler = nextProtocol.newServerAuthHandler();
                        connection.send(AuthStatus.REQUIRED);
                        return;
                    }

                    authenticated = true;
                }
                case MISSING -> {
                    nextProtocol.logErr("Protocol is not loaded on client!");
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }

        // Authenticate client
        if (nextProtocol != null && packet instanceof byte[] secret) {
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
        nextProtocol.logInfo("Connected to: " + connection.getRemoteAddress());
    }
}