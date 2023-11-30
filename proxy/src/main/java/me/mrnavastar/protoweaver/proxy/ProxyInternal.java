package me.mrnavastar.protoweaver.proxy;

import me.mrnavastar.protoweaver.client.ProtoWeaverClient;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyInternal {

    private static final ConcurrentHashMap<InetSocketAddress, ArrayList<ProtoWeaverClient>> clients = new ConcurrentHashMap<>();

    public static void initConnection(InetSocketAddress address, String dir) {
        // Close any existing clients
        ArrayList<ProtoWeaverClient> connections = clients.getOrDefault(address, new ArrayList<>());
        connections.forEach(ProtoWeaverClient::disconnect);
        connections.clear();

        // Connect a client for every protocol
        ProtoProxy.getLoadedProtocols().forEach(protocol -> {
            ProtoWeaverClient client = new ProtoWeaverClient(address, dir + "/protoweaver_hosts");
            client.connectForever(protocol);
            client.onDisconnect(connection -> initConnection(address, dir));
            connections.add(client);
        });

        clients.put(address, connections);
    }

    public static void shutdown() {
        clients.values().forEach(connections -> connections.forEach(ProtoWeaverClient::disconnect));
    }
}