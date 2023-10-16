package me.mrnavastar.singularity.velocity.storage;

import one.microstream.reference.Lazy;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private final ConcurrentHashMap<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    public PlayerData getPlayerData(UUID uuid) {
        PlayerData data = playerData.get(uuid);
        if (data == null) data = new PlayerData();
        return data;
    }

    public void setPlayerData(UUID uuid, PlayerData playerData) {
        this.playerData.put(uuid, playerData);
    }

    public void clearPlayerData(UUID uuid) {
        playerData.remove(uuid);
    }
}