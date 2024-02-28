package me.mrnavastar.protoweaver.proxy;

import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.SocketAddress;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Waterfall extends Plugin implements ServerSupplier, ProtoLogger.IProtoLogger {

    private final Logger logger = getLogger();
    private ProtoProxy protoProxy;

    @Override
    public void onLoad() {
        ProtoLogger.setLogger(this);
    }

    @Override
    public void onEnable() {
        protoProxy = new ProtoProxy(this, getDataFolder().toPath());
    }

    @Override
    public void onDisable() {
        protoProxy.shutdown();
    }

    @Override
    public List<SocketAddress> getServers() {
        return getProxy().getServersCopy().values().stream()
                .map(ServerInfo::getSocketAddress)
                .collect(Collectors.toList());
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