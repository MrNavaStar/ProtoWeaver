package me.mrnavastar.singularity.velocity.storage;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class PlayerData {

    private final ItemStack[] inventory = new ItemStack[46];
    @Setter
    private List<ItemStack> enderChest = new ArrayList<>(27);

    @Setter
    private int selectedSlot = 0;

    @Setter
    private float health = 20;
    @Setter
    private int food = 20;
    @Setter
    private float saturation = 5;

    @Setter
    private float xpBar = 0;
    @Setter
    private int totalXp = 0;
    @Setter
    private int level = 0;

    public void setInventorySlot(int slot, ItemStack itemStack) {
        inventory[slot] = itemStack;
    }

    @Override
    public String toString() {
        return "Inventory: " + Arrays.toString(getInventory())
                + "\nEnder: " + getEnderChest()
                + "\nSelected Slot: " + getSelectedSlot()
                + "\nHealth: " + getHealth()
                + "\nFood: " + getFood() + " S: " + getSaturation();
    }
}