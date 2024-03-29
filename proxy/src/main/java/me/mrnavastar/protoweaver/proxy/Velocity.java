package me.mrnavastar.protoweaver.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(
        id = ProtoConstants.PROTOWEAVER_ID,
        name = ProtoConstants.PROTOWEAVER_NAME,
        version = ProtoConstants.PROTOWEAVER_VERSION,
        authors = "MrNavaStar"
)
public class Velocity implements ServerSupplier, ProtoLogger.IProtoLogger {

    private final Path dir;
    private final ProxyServer velocity;
    private final Logger logger;
    private ProtoProxy protoProxy;

    @Inject
    public Velocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dir) {
        this.velocity = proxyServer;
        this.dir = dir;
        this.logger = logger;
        ProtoLogger.setLogger(this);
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        protoProxy = new ProtoProxy(this, dir);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        protoProxy.shutdown();
    }

    @Override
    public List<SocketAddress> getServers() {
        return velocity.getAllServers().stream()
                .map(server -> server.getServerInfo().getAddress())
                .collect(Collectors.toList());
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }
}