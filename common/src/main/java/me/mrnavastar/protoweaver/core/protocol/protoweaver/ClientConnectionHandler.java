package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

public class ClientConnectionHandler extends InternalConnectionHandler implements ProtoConnectionHandler {

    private boolean authenticated = false;
    private ClientAuthHandler authHandler = null;

    public void start(ProtoConnection connection, String nextProtocolName) {
        Protocol nextProtocol = ProtoWeaver.getLoadedProtocol(nextProtocolName);
        if (nextProtocol == null) {
            protocolNotLoaded(connection, nextProtocolName);
            return;
        }

        authenticated = false;
        if (nextProtocol.requiresAuth(Side.CLIENT)) authHandler = nextProtocol.newClientAuthHandler();
        connection.send(new ProtocolStatus(connection.getProtocol().toString(), nextProtocol.toString(), nextProtocol.hashCode(), ProtocolStatus.Status.START));
    }

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case MISSING -> {
                    ProtoLogger.error("Protocol: \"" + status.getNextProtocol() + "\" is not loaded on server.");
                    disconnectIfNeverUpgraded(connection);
                }
                case MISMATCH -> {
                    ProtoLogger.error("Protocol: \"" + status.getNextProtocol() + "\" has a mismatch with the version on the server!");
                    ProtoLogger.error("Double check that all packets are registered in the same order and all settings are the same.");
                    disconnectIfNeverUpgraded(connection);
                }
                case FULL -> {
                    ProtoLogger.error("Protocol: \"" + status.getNextProtocol() + "\" has reached the maximum number of allowed connections on the server!");
                    disconnectIfNeverUpgraded(connection);
                }
                case UPGRADE -> {
                    if (!authenticated) return;
                    Protocol nextProtocol = ProtoWeaver.getLoadedProtocol(status.getNextProtocol());
                    if (nextProtocol == null) {
                        protocolNotLoaded(connection, status.getNextProtocol());
                        return;
                    }

                    connection.upgradeProtocol(nextProtocol);
                }
            }
            return;
        }

        if (packet instanceof AuthStatus auth) {
            switch (auth) {
                case OK -> authenticated = true;
                case REQUIRED -> {
                    if (authHandler == null) {
                        ProtoLogger.error("Client protocol has not defined an auth handler, but the server requires auth. Closing connection.");
                        connection.disconnect();
                        return;
                    }
                    connection.send(authHandler.getSecret());
                }
                case DENIED -> {
                    ProtoLogger.error("Client was denied access by the server.");
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }
    }
}
