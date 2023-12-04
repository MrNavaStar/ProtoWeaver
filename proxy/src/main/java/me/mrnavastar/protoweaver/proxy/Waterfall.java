package me.mrnavastar.protoweaver.proxy;

import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Waterfall extends Plugin implements ServerSupplier {

    @Override
    public void onLoad() {
        ProtoProxy.setServerSupplier(this);
    }

    @Override
    public void onEnable() {
        ProtoProxy.startAll();
    }

    @Override
    public void onDisable() {
        ProtoProxy.closeAll();
    }

    @Override
    public ArrayList<ServerInfo> getServers() {
        ArrayList<ServerInfo> addresses = new ArrayList<>();
        getProxy().getServersCopy().values().forEach(serverInfo -> {
            addresses.add(new ServerInfo(serverInfo.getName(), (InetSocketAddress) serverInfo.getSocketAddress()));
        });
        return addresses;
    }
}