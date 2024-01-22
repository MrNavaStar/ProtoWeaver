package me.mrnavastar.protoweaver.proxy;

import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Waterfall extends Plugin implements ServerSupplier, ProtoLogger.IProtoLogger {

    private final Logger logger = getLogger();

    @Override
    public void onLoad() {
        ProtoLogger.setLogger(this);
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

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
    }
}