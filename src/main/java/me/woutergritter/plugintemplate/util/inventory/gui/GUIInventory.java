package me.woutergritter.plugintemplate.util.inventory.gui;

import me.woutergritter.plugintemplate.util.inventory.ManagedInventory;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class GUIInventory<T extends GUIParams> {
    private final GUI<T> parent;
    private final T params;
    private final Inventory inventory;

    private boolean opened = false;

    private final Map<String, GUIItem> customItems = new HashMap<>();

    protected GUIInventory(GUI<T> parent, T params, Object... formattedTitleArgs) {
        this.parent = parent;
        this.params = params;

        String title = String.format(parent.title, formattedTitleArgs);
        this.inventory = Bukkit.createInventory(null, parent.height * 9, title);
    }

    public GUIInventory<T> open() {
        if(opened) {
            throw new IllegalStateException("The GUI inventory can only be opened once.");
        }

        opened = true;

        new ManagedInventory(params.player,
                inventory,
                clickEvent -> {
                    String clickedItem = null;
                    if(clickEvent.getClickedInventory() == clickEvent.getView().getTopInventory() &&
                            clickEvent.getCurrentItem() != null) {
                        // Player clicked in top inventory.
                        clickedItem = getItemName(clickEvent.getCurrentItem(), clickEvent.getSlot());
                    }

                    parent.onClickEvent(params, clickedItem, this, clickEvent);
                },
                closeEvent -> {
                    parent.playersInGUI.remove(params.player);

                    parent.onCloseEvent(params, this, closeEvent);
                }
        ).open();

        parent.playersInGUI.put(params.player, this);

        return this;
    }

    public GUIInventory<T> borders(String itemName, Object... formattedItemArgs) {
        for(int x = 0; x < 9; x++) {
            insertSlot(itemName, x, formattedItemArgs);
            insertSlot(itemName, x + (parent.height - 1) * 9, formattedItemArgs);
        }

        for(int y = 1; y < parent.height - 1; y++) {
            insertSlot(itemName, y * 9, formattedItemArgs);
            insertSlot(itemName, y * 9 + 8, formattedItemArgs);
        }

        return this;
    }

    public GUIInventory<T> fill(String itemName, Object... formattedItemArgs) {
        for(int slot = 0; slot < inventory.getSize(); slot++) {
            insertSlot(itemName, slot, formattedItemArgs);
        }

        return this;
    }

    public GUIInventory<T> insertSlot(String itemName, int slot, Object... formattedItemArgs) {
        GUIItem item = getItem(itemName);
        if(item != null) {
            item.insert(inventory, slot, formattedItemArgs);
        }

        if(opened) {
            params.player.updateInventory();
        }

        return this;
    }

    public GUIInventory<T> insert(String itemName, Object... formattedItemArgs) {
        insertSlot(itemName, -1, formattedItemArgs);

        return this;
    }

    public GUIInventory<T> insertAll(String... itemNames) {
        for(String itemName : itemNames) {
            insert(itemName);
        }

        return this;
    }

    public GUIInventory<T> insertRaw(int slot, ItemStack item) {
        inventory.setItem(slot, item);

        if(opened) {
            params.player.updateInventory();
        }

        return this;
    }

    public GUIInventory<T> createCustom(GUIItem item, String itemName) {
        customItems.put(itemName, item);

        return this;
    }

    public GUIItem getItem(String itemName) {
        if(customItems.containsKey(itemName)) {
            return customItems.get(itemName);
        }

        return parent.getItem(itemName);
    }

    public String getItemName(ItemStack lookup, int slot) {
        for(String itemName : customItems.keySet()) {
            GUIItem item = customItems.get(itemName);
            if(item.is(lookup, slot, parent.height * 9)) {
                return itemName;
            }
        }

        return parent.getItemName(lookup, slot);
    }

    public GUI<T> getParent() {
        return parent;
    }

    public T getParams() {
        return params;
    }
}
