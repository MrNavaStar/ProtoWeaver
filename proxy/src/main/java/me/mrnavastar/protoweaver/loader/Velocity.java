package me.mrnavastar.protoweaver.loader;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.proxy.VelocityServer;
import me.mrnavastar.protoweaver.api.ProtoWeaver;
import me.mrnavastar.protoweaver.api.protocol.velocity.VelocityAuth;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.core.util.ProtoLogger;
import me.mrnavastar.protoweaver.loader.netty.SSLContext;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;
import me.mrnavastar.protoweaver.proxy.api.ProtoServer;
import me.mrnavastar.protoweaver.loader.netty.WrappedChannelInitializer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

@Plugin(
        id = ProtoConstants.PROTOWEAVER_ID,
        name = ProtoConstants.PROTOWEAVER_NAME,
        version = ProtoConstants.PROTOWEAVER_VERSION,
        authors = "MrNavaStar"
)
public class Velocity implements ServerSupplier, ProtoLogger.IProtoLogger {

    private final ProxyServer proxy;
    private final Logger logger;
    private final ProtoProxy protoProxy;
    private boolean setup = false;
    @Inject
    public Velocity(ProxyServer proxy, Logger logger, @DataDirectory Path dir) {
        this.proxy = proxy;
        this.logger = logger;
        protoProxy = new ProtoProxy(this, dir);

        if (!ProtoWeaver.getLoadedProtocols().isEmpty()) setup(dir);
        else ProtoWeaver.PROTOCOL_LOADED.register(protocol -> setup(dir));
    }

    private void setup(Path dir) {
        if (setup) return;
        ProtoLogger.setLogger(this);
        WrappedChannelInitializer.install(proxy);

        SSLContext.initKeystore(String.valueOf(dir));
        SSLContext.genKeys();
        SSLContext.initContext();

        VelocityAuth.setSecret(((VelocityServer) proxy).getConfiguration().getForwardingSecret());
        setup = true;
    }

    @Subscribe
    public void onRegister(ServerRegisteredEvent event) {
        ServerInfo server = event.registeredServer().getServerInfo();
        protoProxy.register(new ProtoServer(server.getName(), server.getAddress()));
    }

    @Subscribe
    public void onUnregister(ServerUnregisteredEvent event) {
        ServerInfo server = event.unregisteredServer().getServerInfo();
        protoProxy.unregister(new ProtoServer(server.getName(), server.getAddress()));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        protoProxy.shutdown();
    }

    @Override
    public List<ProtoServer> getServers() {
        return proxy.getAllServers().stream().map(server -> new ProtoServer(server.getServerInfo().getName(), server.getServerInfo().getAddress())).toList();
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    public void err(String message) {
        logger.error(message);
    }
}