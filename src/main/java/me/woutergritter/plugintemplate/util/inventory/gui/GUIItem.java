package me.woutergritter.plugintemplate.util.inventory.gui;

import me.woutergritter.plugintemplate.util.item.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIItem {
    public static final int NO_SLOT = Integer.MAX_VALUE;

    private final ItemStack itemStack;
    private final int relativeSlot;

    public GUIItem(ItemStack itemStack, int relativeSlot) {
        this.itemStack = itemStack;
        this.relativeSlot = relativeSlot;
    }

    public GUIItem(ItemStack itemStack) {
        this(itemStack, NO_SLOT);
    }

    public boolean is(ItemStack other) {
        return ItemUtils.isSimilarFormatted(itemStack, other);
    }

    public boolean is(int slot, int invSize) {
        return getSlot(invSize) == slot;
    }

    public boolean is(ItemStack other, int slot, int invSize) {
        return (relativeSlot == NO_SLOT || is(slot, invSize)) &&
                is(other);
    }

    public boolean insert(Inventory inv, int slot, Object... formattedItemArgs) {
        if(relativeSlot != NO_SLOT) {
            // Override the slot!
            slot = getSlot(inv.getSize());
        }

        if(slot < 0 || slot >= inv.getSize()) {
            return false;
        }

        ItemStack itemStack = ItemUtils.formatItemStack(
                this.itemStack.clone(),
                formattedItemArgs
        );

        inv.setItem(slot, itemStack);

        return true;
    }

    public boolean insert(Inventory inv, Object... formattedItemArgs) {
        return insert(inv, -1, formattedItemArgs);
    }

    public ItemStack getFormattedItemStack(Object... args) {
        return ItemUtils.formatItemStack(itemStack.clone(), args);
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public int getRelativeSlot() {
        return relativeSlot;
    }

    public int getSlot(int invSize) {
        if(relativeSlot == NO_SLOT) {
            return -1;
        }

        int slot = relativeSlot >= 0 ? relativeSlot : invSize - Math.abs(relativeSlot);
        if(slot < 0 || slot >= invSize) {
            return -1;
        }

        return slot;
    }

    public boolean hasSlot() {
        return relativeSlot != NO_SLOT;
    }

    public static GUIItem fromConfig(ConfigurationSection conf) {
        ItemStack itemStack = ItemUtils.fromConfig(conf);
        if(itemStack == null) {
            return null;
        }

        int slot = NO_SLOT;

        String slotStr = conf.getString("slot");
        if(slotStr != null) {
            try{
                slot = Integer.parseInt(slotStr);
            }catch(NumberFormatException e1) {
            }

            if(slot == NO_SLOT) {
                try{
                    String[] parts = slotStr.split("x");

                    int x = Integer.parseInt(parts[0]) - 1;
                    int y = Integer.parseInt(parts[1]) - 1;

                    slot = x + y * 9;
                }catch(Exception e2) {
                    // Invalid slot value
                    return null;
                }
            }
        }

        return new GUIItem(itemStack, slot);
    }
}
