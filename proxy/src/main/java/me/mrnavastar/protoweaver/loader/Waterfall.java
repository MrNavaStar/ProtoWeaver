package me.mrnavastar.protoweaver.loader;

import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;
import me.mrnavastar.protoweaver.proxy.api.ProtoServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;
import java.util.logging.Logger;

public class Waterfall extends Plugin implements ServerSupplier, ProtoLogger.IProtoLogger {

    private final Logger logger = getLogger();
    private final ProtoProxy protoProxy = new ProtoProxy(this, getDataFolder().toPath());

    @Override
    public void onLoad() {
        ProtoLogger.setLogger(this);
    }

    @Override
    public void onDisable() {
        protoProxy.shutdown();
    }

    @Override
    public List<ProtoServer> getServers() {
        return getProxy().getServersCopy().values().stream().map(server -> new ProtoServer(server.getName(), server.getSocketAddress())).toList();
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    public void err(String message) {
        logger.severe(message);
    }
}