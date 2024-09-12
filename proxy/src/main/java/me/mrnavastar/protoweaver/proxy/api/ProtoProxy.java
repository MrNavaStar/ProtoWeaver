package me.mrnavastar.protoweaver.proxy.api;

import lombok.NonNull;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.client.ProtoClient;
import me.mrnavastar.protoweaver.loader.ServerSupplier;
import me.mrnavastar.protoweaver.libs.me.mrnavastar.r.R;

import org.jetbrains.annotations.ApiStatus;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoProxy {

    private static final ConcurrentHashMap<ProtoServer, ArrayList<ProtoClient>> servers = new ConcurrentHashMap<>();

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
        }).onConnectionEstablished(connection -> R.of(server).set("connection", connection));
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
        return servers.keySet().stream().toList();
    }

    /**
     * @return A {@link ProtoServer} that is registered to this proxy instance.
     */
    public static Optional<ProtoServer> getRegisteredServer(String name) {
        return getRegisteredServers().stream().filter(s -> s.getName().equals(name)).findFirst();
    }

    /**
     * @return A {@link ProtoServer} that is registered to this proxy instance.
     */
    public static Optional<ProtoServer> getRegisteredServer(SocketAddress address) {
        return getRegisteredServers().stream().filter(s -> s.getAddress().equals(address)).findFirst();
    }

    /**
     * Returns a list of servers connected on the supplied {@link Protocol}.
     * @param protocol the protocol to check for.
     */
    public static List<ProtoServer> getConnectedServers(@NonNull Protocol protocol) {
        List<ProtoServer> connected = new ArrayList<>();
        servers.forEach((server, clients) -> clients.stream()
                .filter(c -> protocol.equals(c.getCurrentProtocol()) || c.isConnected())
                .findFirst().ifPresent(c -> connected.add(server)));
        return connected;
    }

    /**
     * Returns a {@link ProtoServer} connected on the supplied {@link Protocol}.
     * @param protocol the protocol to check for.
     * @param name the name of the server.
     */
    public static Optional<ProtoServer> getConnectedServer(@NonNull Protocol protocol, String name) {
        return getConnectedServers(protocol).stream().filter(s -> s.getName().equals(name)).findFirst();
    }

    /**
     * Returns a {@link ProtoServer} connected on the supplied {@link Protocol}.
     * @param protocol the protocol to check for.
     * @param address the address of the server.
     */
    public static Optional<ProtoServer> getConnectedServer(@NonNull Protocol protocol, SocketAddress address) {
        return getConnectedServers(protocol).stream().filter(s -> s.getAddress().equals(address)).findFirst();
    }
}