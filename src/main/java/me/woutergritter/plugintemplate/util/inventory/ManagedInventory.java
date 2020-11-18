package me.woutergritter.plugintemplate.util.inventory;

import me.woutergritter.plugintemplate.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ManagedInventory implements Listener {
    private final Inventory inventory;
    private final Player player;

    private final Consumer<InventoryClickEvent> onClick;
    private final Consumer<InventoryCloseEvent> onClose;

    private boolean isOpened = false;
    private boolean isClosed = false;

    public ManagedInventory(Player player, Inventory inventory,
                            Consumer<InventoryClickEvent> onClick, Consumer<InventoryCloseEvent> onClose) {
        this.player = player;
        this.inventory = inventory;

        this.onClick = onClick;
        this.onClose = onClose;
    }

    public ManagedInventory(Player player, String inventoryTitle, ItemStack[] inventoryContents,
                            Consumer<InventoryClickEvent> onClick, Consumer<InventoryCloseEvent> onClose) {
        if(inventoryContents.length % 9 != 0 || inventoryContents.length > 9 * 6) {
            throw new IllegalArgumentException("Invalid inventory contents length.");
        }

        this.player = player;

        this.inventory = Bukkit.createInventory(null, inventoryContents.length, inventoryTitle);
        this.inventory.setContents(inventoryContents);

        this.onClick = onClick;
        this.onClose = onClose;
    }

    public ManagedInventory open() {
        if(isOpened) {
            throw new IllegalStateException("The inventory is or was already opened.");
        }

        isOpened = true;
        player.openInventory(inventory);

        Bukkit.getPluginManager().registerEvents(this, Main.instance());

        return this;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if(e.getWhoClicked() != player) {
            return;
        }

        try{
            onClick.accept(e);
        }catch(Exception ex) {
            Main.instance().getLogger().warning("An exception occurred when processing a GUI click for player " + e.getWhoClicked().getName() + ": " + ex.toString());
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent e) {
        if(e.getPlayer() != player) {
            return;
        }

        try{
            onClose.accept(e);
        }catch(Exception ex) {
            Main.instance().getLogger().warning("An exception occurred when processing a GUI close for player " + e.getPlayer().getName() + ": " + ex.toString());
            ex.printStackTrace();
        }

        HandlerList.unregisterAll(this);

        isClosed = true;
    }

    @EventHandler
    public void onPluginDisableEvent(PluginDisableEvent e) {
        if(e.getPlugin() != Main.instance()) {
            return;
        }

        if(isOpened && !isClosed) {
            player.closeInventory();
        }
    }
}
