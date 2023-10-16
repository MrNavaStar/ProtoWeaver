package me.mrnavastar.singularity.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import lombok.Getter;
import me.mrnavastar.singularity.velocity.storage.DataStore;
import me.mrnavastar.singularity.velocity.networking.PacketListener;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = BuildConstants.MOD_ID,
        name = BuildConstants.MOD_NAME,
        version = BuildConstants.VERSION
)
public class Velocity {

    @Inject
    private Logger logger;
    @DataDirectory
    private Path dir;
    @Inject
    private ProxyServer server;

    @Getter
    private static EmbeddedStorageManager storageManager;
    @Getter
    private static final DataStore dataStore = new DataStore();

    @Subscribe(order = PostOrder.LAST)
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Warping Space and Time...");

        // Init Storage
        /*storageManager = EmbeddedStorage.start(dataStore);
        storageManager.storeRoot();*/

        // Init Packet API
        //PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, server.getPluginManager().ensurePluginContainer(this)));
        PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(false).debug(true).reEncodeByDefault(true);
        //PacketEvents.getAPI().load();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());
        //PacketEvents.getAPI().init();
    }

    @Subscribe
    private void onProxyShutdown(ProxyShutdownEvent event) {
        PacketEvents.getAPI().terminate();
        //storageManager.shutdown();
    }

    @Subscribe
    private void onLogin(PostLoginEvent event) {
        //event.getPlayer().sendPluginMessage();
        System.out.println(dataStore.getPlayerData(event.getPlayer().getUniqueId()));
    }
}