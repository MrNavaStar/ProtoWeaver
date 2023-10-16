package me.mrnavastar.singularity.velocity.networking;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import me.mrnavastar.singularity.velocity.Velocity;
import me.mrnavastar.singularity.velocity.storage.PlayerData;

import java.util.Objects;
import java.util.UUID;

public class PacketListener extends PacketListenerAbstract {

    private int enderWindowId = -1;

    // Sever to Client Packets
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == null) return;

        UUID playerUuid = event.getUser().getUUID();
        if (playerUuid == null) return;
        PlayerData playerData = Velocity.getDataStore().getPlayerData(playerUuid);

        // Override server held item slot with known held item slot
        /*if (event.getPacketType() == PacketType.Play.Server.HELD_ITEM_CHANGE) {
            WrapperPlayServerHeldItemChange heldItemPacket = new WrapperPlayServerHeldItemChange(event);

            //heldItemPacket.setSlot(playerData.getSelectedSlot());
            System.out.println("Hello??");
        }*/

        // Item in slot changes
        /*if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
            System.out.println("pog");

            WrapperPlayServerSetSlot setSlotPacket = new WrapperPlayServerSetSlot(event);

            if (setSlotPacket.getWindowId() == 0) {
                playerData.setInventorySlot(setSlotPacket.getSlot(), setSlotPacket.getItem());
                Velocity.getDataStore().setPlayerData(playerUuid, playerData);
                //Velocity.getStorageManager().store(playerData);
            }
        }*/

        // Check if opened window was an ender chest
        /*else if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
            CustomWrapperPlayServerOpenWindow openWindowPacket = new CustomWrapperPlayServerOpenWindow(event);

            if (Objects.equals(openWindowPacket.getTitle(), "{\"translate\":\"container.enderchest\"}")) {
                enderWindowId = openWindowPacket.getContainerId();
            }
        }*/

        // Read data from window as ender chest if window ids match
        /*else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
            WrapperPlayServerWindowItems windowItemsPacket = new WrapperPlayServerWindowItems(event);

            if (windowItemsPacket.getWindowId() == enderWindowId) {
                playerData.setEnderChest(windowItemsPacket.getItems());
                enderWindowId = -1;
            }
        }*/

        // Health / Food is updated
       /* else if (event.getPacketType() == PacketType.Play.Server.UPDATE_HEALTH) {
            WrapperPlayServerUpdateHealth healthPacket = new WrapperPlayServerUpdateHealth(event);

            playerData.setFood(healthPacket.getFood());
            playerData.setSaturation(healthPacket.getFoodSaturation());
            playerData.setHealth(healthPacket.getHealth());
            Velocity.getDataStore().setPlayerData(playerUuid, playerData);
            //Velocity.getStorageManager().store(playerData);
        }

        // Player death
        else if (event.getPacketType() == PacketType.Play.Server.DEATH_COMBAT_EVENT) {
            Velocity.getDataStore().clearPlayerData(playerUuid);
            //Velocity.getStorageManager().store(playerData);
        }

        else if (event.getPacketType() == PacketType.Play.Server.EFFECT) {

        }

        else if (event.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
            WrapperPlayServerEntityEffect entityEffect = new WrapperPlayServerEntityEffect(event);

        }

        else if (event.getPacketType() == PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
            WrapperPlayServerRemoveEntityEffect removeEntityEffect = new WrapperPlayServerRemoveEntityEffect(event);

        }

        // Xp is updated
        else if (event.getPacketType() == PacketType.Play.Server.SET_EXPERIENCE) {
            WrapperPlayServerSetExperience xpPacket = new WrapperPlayServerSetExperience(event);

            playerData.setLevel(xpPacket.getLevel());
            playerData.setTotalXp(xpPacket.getTotalExperience());
            playerData.setXpBar(xpPacket.getExperienceBar());
            Velocity.getDataStore().setPlayerData(playerUuid, playerData);
           // Velocity.getStorageManager().store(playerData);
        }*/
    }

    // Client to Server Packets
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == null) return;

        UUID playerUuid = event.getUser().getUUID();
        if (playerUuid == null) return;
        PlayerData playerData = Velocity.getDataStore().getPlayerData(playerUuid);

        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            WrapperPlayClientHeldItemChange heldItemPacket = new WrapperPlayClientHeldItemChange(event);
            playerData.setSelectedSlot(heldItemPacket.getSlot());
            Velocity.getDataStore().setPlayerData(playerUuid, playerData);
            //Velocity.getStorageManager().store(playerData);
        }

        // Player drops item (Q)
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging playerDigging = new WrapperPlayClientPlayerDigging(event);

            if (playerDigging.getAction().equals(DiggingAction.DROP_ITEM)) {
                System.out.println("DROP");
                /*playerData.getInventory()[playerData.getSelectedSlot()].shrink(1);
                Velocity.getDataStore().setPlayerData(playerUuid, playerData);*/
            }

            else if (playerDigging.getAction().equals(DiggingAction.DROP_ITEM_STACK)) {
                System.out.println("DROP STACK");
               /* playerData.setInventorySlot(playerData.getSelectedSlot(), null);
                Velocity.getDataStore().setPlayerData(playerUuid, playerData);*/
            }
        }
    }
}
