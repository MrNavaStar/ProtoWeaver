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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ProtoProxy {

    private static final ConcurrentHashMap<ProtoServer, ArrayList<ProtoClient>> servers = new ConcurrentHashMap<>();
    private static ServerSupplier supplier;

    /**
     * Sets the polling rate of servers that are disconnected. Defaults to 5 seconds
     */
    @Setter
    private static int serverPollRate = 5000;
    private final String hostsFile;

    @ApiStatus.Internal
    public ProtoProxy(ServerSupplier serverSupplier, Path dir) {
        supplier = serverSupplier;
        this.hostsFile = dir.toAbsolutePath().toString();
        supplier.getServers().forEach(server -> servers.put(server, new ArrayList<>()));
        ProtoWeaver.PROTOCOL_LOADED.register(this::startProtocol);
        ProtoWeaver.getLoadedProtocols().forEach(this::startProtocol);
    }

    private void startProtocol(Protocol protocol) {
        if (protocol.toString().equals("protoweaver:internal")) return;

        servers.forEach((server, clients) -> {
            for (ProtoClient client : clients) {
                // Don't start a new connection if one already exists for this protocol
                if (client.getCurrentProtocol().toString().equals(protocol.toString())) return;
            }
            connectClient(protocol, server, clients);
        });
    }

    private void connectClient(Protocol protocol, ProtoServer server, ArrayList<ProtoClient> clients) {
        ProtoClient client = new ProtoClient((InetSocketAddress) server.getAddress(), hostsFile);
        client.connect(protocol).onConnectionLost(connection -> {
            clients.remove(client);

            if (connection.getDisconnecter().equals(Side.CLIENT)) return;
            Thread.sleep(serverPollRate);
            connectClient(protocol, server, clients);
        }).onConnectionEstablished(connection -> {
            ProtoLogger.info("Connected to: " + server + " with protocol: " + protocol);
        });
        clients.add(client);
    }

    @ApiStatus.Internal
    public void shutdown() {
        servers.values().forEach(clients -> clients.forEach(ProtoClient::disconnect));
        servers.clear();
    }

    @ApiStatus.Internal
    public void register(ProtoServer server) {
        if (servers.putIfAbsent(server, new ArrayList<>()) == null)
            ProtoWeaver.getLoadedProtocols().forEach(this::startProtocol);
    }

    @ApiStatus.Internal
    public void unregister(ProtoServer server) {
        Optional.ofNullable(servers.remove(server)).ifPresent(clients -> clients.forEach(ProtoClient::disconnect));
    }

    /**
     * @return A list of {@link ProtoServer} that are registered to this proxy instance.
     */
    public static List<ProtoServer> getRegisteredServers() {
        return supplier.getServers();
    }

    /**
     * Returns a list of servers connected on the supplied {@link Protocol}.
     * @param protocol the protocol to check for.
     */
    public static List<ProtoServer> getConnectedServers(@NonNull Protocol protocol) {
        List<ProtoServer> connected = new ArrayList<>();
        servers.forEach((server, clients) -> clients.stream()
                .filter(c -> protocol.equals(c.getCurrentProtocol()) || c.isConnected())
                .findFirst().ifPresent(c -> connected.add(new ProtoServer(server, c.getConnection()))));
        return connected;
    }

    /**
     * Sends a packet to every server running protoweaver with the correct protocol.
     */
    public static void send(@NonNull Protocol protocol, @NonNull Object packet) {
        servers.values().forEach(clients -> clients.forEach(client -> {
            if (protocol.equals(client.getCurrentProtocol())) client.send(packet);
        }));
    }

    /**
     * Sends a packet to a specific server.
     * @return true if success, false if failure or the server doesn't have the relevant protocol loaded
     */
    public static boolean send(@NonNull Protocol protocol, @NonNull InetSocketAddress address, @NonNull Object packet) {
        for (ProtoClient client : servers.get(address)) {
            if (protocol.equals(client.getCurrentProtocol())) return client.send(packet).isSuccess();
        }
        return false;
    }
}