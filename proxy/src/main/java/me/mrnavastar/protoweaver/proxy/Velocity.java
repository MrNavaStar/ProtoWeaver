package me.mrnavastar.protoweaver.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = ProtoConstants.PROTOWEAVER_ID,
        name = ProtoConstants.PROTOWEAVER_NAME,
        version = "debug",
        authors = "MrNavaStar"
)
public class Velocity {

    private final Logger logger;
    private final Path dir;
    private final ProxyServer server;

    @Inject
    public Velocity(ProxyServer server, Logger logger, @DataDirectory Path dir) {
        this.server = server;
        this.logger = logger;
        this.dir = dir;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getAllServers().forEach(server -> ProxyInternal.initConnection(server.getServerInfo().getAddress(), dir.toString()));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        ProxyInternal.shutdown();
    }
}