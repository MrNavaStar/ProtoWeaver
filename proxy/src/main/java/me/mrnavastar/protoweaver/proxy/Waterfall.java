package me.mrnavastar.protoweaver.proxy;

import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;

public class Waterfall extends Plugin {

    @Override
    public void onEnable() {
        getProxy().getServersCopy().values().forEach(serverInfo -> ProxyInternal.initConnection((InetSocketAddress) serverInfo.getSocketAddress(), getDataFolder().getAbsolutePath()));
    }

    @Override
    public void onDisable() {
        ProxyInternal.shutdown();
    }
}