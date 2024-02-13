package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.auth.ServerAuthHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

public class ServerConnectionHandler extends InternalConnectionHandler implements ProtoConnectionHandler {

    private boolean authenticated = false;
    private Protocol nextProtocol = null;
    private ServerAuthHandler authHandler = null;

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case START -> {
                    // Check if protocol loaded
                    nextProtocol = loadedProtocols.get(status.getNextProtocol());
                    if (nextProtocol == null) {
                        protocolNotLoaded(connection, status.getNextProtocol());
                        return;
                    }

                    // Check if protocol needs authentication
                    if (nextProtocol.getServerAuthHandler() != null) {
                        authHandler = nextProtocol.newServerAuthHandler();
                        connection.send(new AuthStatus(AuthStatus.Status.REQUIRED));
                        return;
                    }

                    authenticated = true;
                }
                case MISSING -> {
                    ProtoLogger.error("Protocol: \"" + status.getNextProtocol() + "\" is not loaded on client.");
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }

        // Authenticate client
        if (nextProtocol != null && packet instanceof ClientSecret auth) {
            authenticated = authHandler.handleAuth(connection, auth.getSecret());
        }

        if (!authenticated) {
            Sender sender = connection.send(new AuthStatus(AuthStatus.Status.DENIED));
            disconnectIfNeverUpgraded(connection, sender);
            return;
        }

        // Upgrade protocol
        connection.send(new AuthStatus(AuthStatus.Status.OK));
        connection.send(new ProtocolStatus(connection.getProtocol().getName(), nextProtocol.getName(), ProtocolStatus.Status.UPGRADE));
        connection.upgradeProtocol(nextProtocol);
    }
}