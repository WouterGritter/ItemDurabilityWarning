package me.woutergritter.plugintemplate.util.inventory.gui;

import me.woutergritter.plugintemplate.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class GUI<T extends GUIParams> {
    protected final ConfigurationSection conf;

    protected String title;
    protected int height;

    private final Map<String, GUIItem> items = new HashMap<>();

    protected final Map<Player, GUIInventory<T>> playersInGUI = new HashMap<>();

    public GUI(ConfigurationSection conf) {
        this.conf = conf;

        this.title = ChatColor.translateAlternateColorCodes('&', conf.getString("title", "Title"));
        this.height = conf.getInt("height", 3);

        ConfigurationSection itemsConf = conf.getConfigurationSection("items");
        itemsConf.getKeys(false).forEach(key -> {
            ConfigurationSection itemConf = itemsConf.getConfigurationSection(key);
            boolean success = loadItem(itemConf);
            if(!success) {
                Main.instance().getLogger().warning("Could not load item '" + key + "' for GUI '" + title + "'.");
            }
        });
    }

    public abstract void open(T params);

    protected abstract void onClickEvent(T params, String clickedItem, GUIInventory<T> gui, InventoryClickEvent e);

    protected abstract void onCloseEvent(T params, GUIInventory<T> gui, InventoryCloseEvent e);

    protected GUIInventory<T> create(T params, Object... formattedTitleArgs) {
        return new GUIInventory<T>(this, params, formattedTitleArgs);
    }

    protected void createItem(GUIItem item, String itemName) {
        items.put(itemName, item);
    }

    protected GUIItem getItem(String itemName) {
        return items.get(itemName);
    }

    protected String getItemName(ItemStack lookup, int slot) {
        for(String itemName : items.keySet()) {
            GUIItem item = items.get(itemName);
            if(item.is(lookup, slot, height * 9)) {
                return itemName;
            }
        }

        return null;
    }

    public Map<Player, GUIInventory<T>> getPlayersInGUI() {
        return Collections.unmodifiableMap(playersInGUI);
    }

    private boolean loadItem(ConfigurationSection itemConf) {
        String itemName = itemConf.getName().toLowerCase();

        if(items.containsKey(itemName)) {
            // Duplicate!
            return false;
        }

        GUIItem item = GUIItem.fromConfig(itemConf);
        if(item == null) {
            // Invalid config!
            return false;
        }

        items.put(itemName, item);

        return true;
    }
}
