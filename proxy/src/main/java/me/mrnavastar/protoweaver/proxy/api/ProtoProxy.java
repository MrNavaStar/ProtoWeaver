package me.mrnavastar.protoweaver.proxy.api;

import lombok.NonNull;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.client.ProtoClient;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.proxy.ServerSupplier;
import org.jetbrains.annotations.ApiStatus;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoProxy {

    private static final ConcurrentHashMap<SocketAddress, ArrayList<ProtoClient>> servers = new ConcurrentHashMap<>();

    /**
     * Sets the polling rate of servers that are disconnected. Defaults to 5 seconds
     */
    @Setter
    private static int serverPollRate = 5000;
    private final String hostsFile;

    @ApiStatus.Internal
    public ProtoProxy(ServerSupplier serverSupplier, Path dir) {
        this.hostsFile = dir.toAbsolutePath().toString();
        serverSupplier.getServers().forEach(server -> servers.put(server, new ArrayList<>()));
        ProtoWeaver.PROTOCOL_LOADED.register(this::startProtocol);
        ProtoWeaver.getLoadedProtocols().forEach(this::startProtocol);
    }

    private void startProtocol(Protocol protocol) {
        if (protocol.toString().equals("protoweaver:internal")) return;

        servers.forEach((address, clients) -> {
            for (ProtoClient client : clients) {
                // Don't start a new connection if one already exists for this protocol
                if (client.getCurrentProtocol().toString().equals(protocol.toString())) return;
            }
            connectClient(protocol, address, clients);
        });
    }

    private void connectClient(Protocol protocol, SocketAddress address, ArrayList<ProtoClient> clients) {
        ProtoClient client = new ProtoClient((InetSocketAddress) address, hostsFile);
        client.connect(protocol).onConnectionLost(connection -> {
            clients.remove(client);

            if (connection.getDisconnecter().equals(Side.CLIENT)) return;
            Thread.sleep(serverPollRate);
            connectClient(protocol, address, clients);
        }).onConnectionEstablished(connection -> ProtoLogger.info("Connected to: " + address + " with protocol: " + protocol));
        clients.add(client);
    }

    @ApiStatus.Internal
    public void shutdown() {
        servers.values().forEach(clients -> clients.forEach(ProtoClient::disconnect));
        servers.clear();
    }

    /**
     * Sends a packet to every server running protoweaver with the correct protocol.
     */
    public static void sendAll(@NonNull Object packet) {
        servers.values().forEach(clients -> clients.forEach(client -> client.send(packet)));
    }

    /**
     * Sends a packet to a specific server.
     * @return true if success, false if failure or the server doesn't have the relevant protocol loaded
     */
    public static boolean send(@NonNull InetSocketAddress address, @NonNull Object packet) {
        for (ProtoClient client : servers.get(address)) {
            Sender s = client.send(packet);
            if (s.isSuccess()) return true;
        }
        return false;
    }
}