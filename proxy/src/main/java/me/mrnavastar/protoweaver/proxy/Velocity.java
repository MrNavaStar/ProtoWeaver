package me.mrnavastar.protoweaver.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.mrnavastar.protoweaver.api.protocol.protomessage.ProtoMessage;
import me.mrnavastar.protoweaver.core.util.ProtoConstants;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;

import java.nio.file.Path;
import java.util.ArrayList;

@Plugin(
        id = ProtoConstants.PROTOWEAVER_ID,
        name = ProtoConstants.PROTOWEAVER_NAME,
        version = "debug-build",
        authors = "MrNavaStar"
)
public class Velocity implements ServerSupplier {

    private final Path dir;
    private final ProxyServer proxy;

    @Inject
    public Velocity(ProxyServer proxyServer, @DataDirectory Path dir) {
        this.proxy = proxyServer;
        this.dir = dir;
        ProtoProxy.setServerSupplier(this);
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        ProtoProxy.startAll();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        ProtoProxy.closeAll();
    }

    @Override
    public ArrayList<ServerInfo> getServers() {
        ArrayList<ServerInfo> addresses = new ArrayList<>();
        proxy.getAllServers().forEach(sever -> {
            addresses.add(new ServerInfo(sever.getServerInfo().getName(), sever.getServerInfo().getAddress()));
        });
        return addresses;
    }
}