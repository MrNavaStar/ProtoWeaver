package me.mrnavastar.protoweaver.proxy.api;

import lombok.NonNull;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.netty.Sender;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.api.protocol.Side;
import me.mrnavastar.protoweaver.client.ProtoWeaverClient;
import me.mrnavastar.protoweaver.proxy.ServerSupplier;
import org.jetbrains.annotations.ApiStatus;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ProtoProxy {

    private static final ConcurrentHashMap<InetSocketAddress, ArrayList<ProtoWeaverClient>> backendServers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, InetSocketAddress> backendServerLookup = new ConcurrentHashMap<>();

    /**
     * Sets the polling rate of servers that are disconnected. Defaults to 5 seconds
     */
    @Setter
    private static int serverPollRate = 5000;

    public ProtoProxy(ServerSupplier serverSupplier) {
        closeAll();
        serverSupplier.getServers().forEach(serverInfo -> {
            backendServers.put(serverInfo.getAddress(), new ArrayList<>());
            backendServerLookup.put(serverInfo.getName(), serverInfo.getAddress());
        });

        ProtoWeaver.PROTOCOL_LOADED.register(ProtoProxy::startProtocol);
    }

    private static void startProtocol(Protocol protocol) {
        backendServers.forEach((address, clients) -> {
            for (ProtoWeaverClient client : clients) {
                // Don't start a new connection if one already exists for this protocol
                if (client.getCurrentProtocol().getName().equals(protocol.getName())) return;
            }
            connectClient(protocol, address, clients);
        });
    }

    private static void connectClient(Protocol protocol, InetSocketAddress address, ArrayList<ProtoWeaverClient> clients) {
        ProtoWeaverClient client = new ProtoWeaverClient(address);
        client.connect(protocol).onConnectionLost(connection -> {
            if (connection.getDisconnecter().equals(Side.CLIENT)) return;

            clients.remove(client);
            Thread.sleep(serverPollRate);
            connectClient(protocol, address, clients);
        });
        clients.add(client);
    }

    @ApiStatus.Internal
    public void closeAll() {
        backendServers.values().forEach(clients -> clients.forEach(ProtoWeaverClient::disconnect));
    }

    @ApiStatus.Internal
    public void startAll() {
        ProtoWeaver.getLoadedProtocols().forEach(ProtoProxy::startProtocol);
    }

    /**
     * Sends a packet to every server running protoweaver with the correct protocol.
     */
    public static void sendAll(@NonNull Object packet) {
        backendServers.values().forEach(clients -> clients.forEach(client -> client.send(packet)));
    }

    /**
     * Sends a packet to a specific server. Does nothing if the server doesn't have the relevant protocol loaded.
     */
    public static Sender send(@NonNull InetSocketAddress address, @NonNull Object packet) {
        for (ProtoWeaverClient client : backendServers.get(address)) {
            Sender s = client.send(packet);
            if (s.isSuccess()) return s;
        }
        return Sender.NULL;
    }

    /**
     * Sends a packet to a specific server. Does nothing if the server doesn't have the relevant protocol loaded.
     * @return True if name is valid, false if invalid
     */
    public static boolean send(@NonNull String serverName, @NonNull Object packet) {
        InetSocketAddress address = backendServerLookup.get(serverName);
        if (address == null) return false;
        send(address, packet);
        return true;
    }
}