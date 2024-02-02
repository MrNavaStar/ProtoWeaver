package me.mrnavastar.protoweaver.proxy.api;

import io.netty.channel.ChannelFutureListener;
import lombok.NonNull;
import lombok.Setter;
import me.mrnavastar.protoweaver.api.ProtoPacket;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.client.ProtoWeaverClient;
import me.mrnavastar.protoweaver.proxy.ServerSupplier;

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

    /**
     * Sets the supplier that will be used to get info about the backend minecraft servers (their ip addresses and names)
     * This is used internally to provide multiplatform support. Only use this function if you are 100% sure you know what
     * you are doing, as it can easily break other plugins if miss-used.
     */
    public static void setServerSupplier(ServerSupplier serverSupplier) {
        closeAll();
        serverSupplier.getServers().forEach(serverInfo -> {
            backendServers.put(serverInfo.getAddress(), new ArrayList<>());
            backendServerLookup.put(serverInfo.getName(), serverInfo.getAddress());
        });
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
        client.connect(protocol).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) return;

            clients.remove(client);
            Thread.sleep(serverPollRate);
            connectClient(protocol, address, clients);
        });
        client.onConnectionLost(connection -> connectClient(protocol, address, clients));

        clients.add(client);
    }

    /**
     * Loads the given protocol and opens connections to every backend server that support the protocol.
     */
    public static void load(Protocol protocol) {
        ProtoWeaver.load(protocol);
        startProtocol(protocol);
    }

    /**
     * Closes all connections to the backend servers, including ones opened by other plugins.
     * Internally this only called when the proxy shuts down.
     * Calling {@link ProtoProxy#startAll()} will reopen the connections.
     */
    public static void closeAll() {
        backendServers.values().forEach(clients -> clients.forEach(ProtoWeaverClient::disconnect));
    }

    /**
     * Opens all connections to the backend servers for every protocol loaded in the current jvm instance.
     * Calling {@link ProtoProxy#closeAll()} will close the connections.
     */
    public static void startAll() {
        ProtoWeaver.getLoadedProtocols().forEach(ProtoProxy::startProtocol);
    }

    /**
     * Sends a packet to every server running protoweaver with the correct protocol.
     */
    public static void sendAll(@NonNull ProtoPacket packet) {
        backendServers.values().forEach(clients -> clients.forEach(client -> {
            try {
                client.send(packet);
            } catch (Exception ignore) {}
        }));
    }

    /**
     * Sends a packet to a specific server. Does nothing if the server doesn't have the relevant protocol loaded.
     */
    public static void send(@NonNull InetSocketAddress address, @NonNull ProtoPacket packet) {
        backendServers.values().forEach(clients -> clients.forEach(client -> {
            if (!address.equals(client.getAddress())) return;
            try {
                client.send(packet);
            } catch (Exception ignore) {}
        }));
    }

    /**
     * Sends a packet to a specific server. Does nothing if the server doesn't have the relevant protocol loaded.
     * @return True if name is valid, false if invalid
     */
    public static boolean send(@NonNull String serverName, @NonNull ProtoPacket packet) {
        InetSocketAddress address = backendServerLookup.get(serverName);
        if (address == null) return false;
        send(address, packet);
        return true;
    }
}