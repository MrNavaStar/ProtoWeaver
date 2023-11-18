package me.mrnavastar.protoweaver.core.protocol.protoweaver;

import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoPacketHandler;
import me.mrnavastar.protoweaver.api.auth.ClientAuthHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;

public class ClientHandler extends InternalProtocol implements ProtoPacketHandler {

    private boolean authenticated = false;
    private ClientAuthHandler authHandler = null;

    public void start(ProtoConnection connection, String nextProtocolName) {
        Protocol nextProtocol = loadedProtocols.get(nextProtocolName);
        if (nextProtocol == null) {
            protocolNotLoaded(connection, nextProtocolName);
            return;
        }

        authenticated = false;
        if (nextProtocol.getClientAuthHandler() != null) authHandler = nextProtocol.newClientAuthHandler();
        connection.send(new ProtocolStatus(connection.getProtocol().getName(), nextProtocol.getName(), ProtocolStatus.Status.START));
    }

    @Override
    public void handlePacket(ProtoConnection connection, ProtoPacket packet) {
        if (packet instanceof ProtocolStatus status) {
            switch (status.getStatus()) {
                case MISSING -> {
                    ProtoLogger.error("Protocol: \"" + status.getNextProtocol() + "\" is not loaded on server.");
                    disconnectIfNeverUpgraded(connection);
                }
                case UPGRADE -> {
                    if (!authenticated) return;
                    Protocol nextProtocol = loadedProtocols.get(status.getNextProtocol());
                    if (nextProtocol == null) {
                        protocolNotLoaded(connection, status.getNextProtocol());
                        return;
                    }

                    connection.upgradeProtocol(nextProtocol);
                }
            }
        }
        else if (packet instanceof AuthStatus auth) {
            switch (auth.getStatus()) {
                case OK -> authenticated = true;
                case REQUIRED -> {
                    if (authHandler == null) {
                        ProtoLogger.error("Client protocol has not defined an auth handler, but the server requires auth. Closing connection.");
                        connection.disconnect();
                        return;
                    }
                    connection.send(new ClientSecret(authHandler.getSecret()));
                }
                case DENIED -> {
                    ProtoLogger.error("Client was denied access by the server.");
                    disconnectIfNeverUpgraded(connection);
                }
            }
        }
    }
}
