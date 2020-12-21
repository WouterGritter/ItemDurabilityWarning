package me.woutergritter.itemdurabilitywarning.itemwarning;

import me.woutergritter.itemdurabilitywarning.Main;
import me.woutergritter.itemdurabilitywarning.Permissions;
import me.woutergritter.itemdurabilitywarning.util.string.StringUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemWarningService implements Listener {
    private final boolean cfg_defaultEnabled;
    private final double cfg_subtleWarningPercent;
    private final double cfg_largeWarningPercent;
    private final List<String> cfg_materialFilter;

    private final NamespacedKey warningsEnabledKey;
    private final NamespacedKey warningsIgnoredKey;

    private final List<Player> playersWithLargeWarning = new ArrayList<>();

    public ItemWarningService() {
        cfg_defaultEnabled = Main.instance().getConfig().getBoolean("default-enabled");
        cfg_subtleWarningPercent = Main.instance().getConfig().getDouble("subtle-warning-percent") / 100.0; // Convert from 0-100 > 0-1
        cfg_largeWarningPercent = Main.instance().getConfig().getDouble("large-warning-percent") / 100.0; // Convert from 0-100 > 0-1
        cfg_materialFilter = Main.instance().getConfig().getStringList("material-filter").stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        warningsEnabledKey = new NamespacedKey(Main.instance(), "warnings_enabled");
        warningsIgnoredKey = new NamespacedKey(Main.instance(), "warnings_ignored");

        Bukkit.getScheduler().runTaskTimer(Main.instance(), this::tick, 10, 10);
        Bukkit.getPluginManager().registerEvents(this, Main.instance());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        playersWithLargeWarning.remove(e.getPlayer());
    }

    private void tick() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(isWarningsEnabled(player)) {
                // The player has item durability warnings enabled..

                Map<WarningType, List<ItemStack>> warnings = calculateWarningTypes(
                        player.getInventory().getItemInMainHand(),
                        player.getInventory().getItemInOffHand(),
                        player.getInventory().getHelmet(),
                        player.getInventory().getChestplate(),
                        player.getInventory().getLeggings(),
                        player.getInventory().getBoots()
                );

                if(warnings.containsKey(WarningType.LARGE)) {
                    // Large warning!

                    int fadeIn = 0;
                    int stay = 15;
                    int fadeOut = 10;

                    if(!playersWithLargeWarning.contains(player)) {
                        playersWithLargeWarning.add(player);

                        // Player didn't have this warning before..
                        fadeIn = 10;

                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.9f);
                        Bukkit.getScheduler().runTaskLater(Main.instance(),
                                () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.9f),
                                2
                        );
                    }

                    String itemWarningString = createItemWarningString(warnings.get(WarningType.LARGE));

                    player.sendTitle(
                            Main.instance().getLang().getMessage("warning.large.title", itemWarningString),
                            Main.instance().getLang().getMessage("warning.large.sub-title", itemWarningString),
                            fadeIn, stay, fadeOut
                    );
                }else{
                    playersWithLargeWarning.remove(player);
                }

                if(warnings.containsKey(WarningType.SUBTLE)) {
                    // Subtle warning!

                    String itemWarningString = createItemWarningString(warnings.get(WarningType.SUBTLE));

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                            Main.instance().getLang().getMessage("warning.subtle", itemWarningString)));
                }
            }
        });
    }

    private WarningType calculateWarningType(ItemStack item) {
        if(item == null) {
            return WarningType.NONE;
        }

        if(item.getType().getMaxDurability() > 0 && item.getItemMeta() instanceof Damageable && !isItemWarningsIgnored(item)) {
            Damageable damageableMeta = (Damageable) item.getItemMeta();
            double damageLeft = 1.0 - (double) damageableMeta.getDamage() / (double) item.getType().getMaxDurability();

            if(damageLeft < cfg_largeWarningPercent) {
                return WarningType.LARGE;
            }else if(damageLeft < cfg_subtleWarningPercent) {
                return WarningType.SUBTLE;
            }
        }

        return WarningType.NONE;
    }

    private Map<WarningType, List<ItemStack>> calculateWarningTypes(ItemStack... items) {
        Map<WarningType, List<ItemStack>> res = new HashMap<>();
        for(ItemStack item : items) {
            WarningType itemWarning = calculateWarningType(item);
            res.computeIfAbsent(itemWarning, k -> new ArrayList<>())
                    .add(item);
        }

        return res;
    }

    private String createItemWarningString(List<ItemStack> items) {
        return items.stream()
                .map(item -> {
                    ItemMeta itemMeta = item.getItemMeta();

                    String name = itemMeta.hasDisplayName() ? ChatColor.stripColor(itemMeta.getDisplayName()) : StringUtils.prettifyString(item.getType().name());
                    double durabilityPercent = 1.0 - (double) ((Damageable) itemMeta).getDamage() / (double) item.getType().getMaxDurability();

                    return Main.instance().getLang().getMessage("warning.item-entry", name, durabilityPercent * 100.0);
                })
                .collect(Collectors.joining(Main.instance().getLang().getMessage("warning.entry-delimiter")));
    }

    public void setWarningsEnabled(Player player, boolean enabled) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(warningsEnabledKey, PersistentDataType.BYTE, enabled ? (byte) 1 : (byte) 0);
    }

    public boolean isWarningsEnabled(Player player) {
        if(!player.hasPermission(Permissions.ITEMWARNINGS)) {
            return false;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();

        Byte enabled = pdc.get(warningsEnabledKey, PersistentDataType.BYTE);
        if(enabled == null) {
            return cfg_defaultEnabled;
        }

        return enabled != (byte) 0;
    }

    public void setItemWarningsIgnored(ItemStack itemStack, boolean ignored) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) {
            throw new NullPointerException("The ItemStack must have a valid ItemMeta.");
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        if(ignored) {
            pdc.set(warningsIgnoredKey, PersistentDataType.BYTE, (byte) 1);
        }else{
            pdc.remove(warningsIgnoredKey);
        }

        itemStack.setItemMeta(itemMeta);
    }

    public boolean isItemWarningsIgnored(ItemStack itemStack) {
        if(!cfg_materialFilter.isEmpty()) {
            // Check if the material filter contains this item
            String itemName = itemStack.getType().name();
            boolean contains = false;
            for(String filter : cfg_materialFilter) {
                if(itemName.contains(filter)) {
                    contains = true;
                    break;
                }
            }

            if(!contains) {
                return true; // Ignored!
            }
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) {
            throw new NullPointerException("The ItemStack must have a valid ItemMeta.");
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        Byte ignored = pdc.get(warningsIgnoredKey, PersistentDataType.BYTE);
        if(ignored == null) {
            return false;
        }

        return ignored != (byte) 0;
    }
}
