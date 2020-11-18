package me.woutergritter.itemdurabilitywarning.itemwarning;

import me.woutergritter.itemdurabilitywarning.Main;
import me.woutergritter.itemdurabilitywarning.Permissions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import java.util.List;

public class ItemWarningService implements Listener {
    private final boolean cfg_defaultEnabled;
    private final double cfg_subtleWarningPercent;
    private final double cfg_largeWarningPercent;

    private final NamespacedKey warningsEnabledKey;
    private final NamespacedKey warningsIgnoredKey;

    private final List<Player> playersWithLargeWarning = new ArrayList<>();

    public ItemWarningService() {
        cfg_defaultEnabled = Main.instance().getConfig().getBoolean("default-enabled");
        cfg_subtleWarningPercent = Main.instance().getConfig().getDouble("subtle-warning-percent") / 100.0; // Convert from 0-100 > 0-1
        cfg_largeWarningPercent = Main.instance().getConfig().getDouble("large-warning-percent") / 100.0; // Convert from 0-100 > 0-1

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
            boolean hasLargeWarning = false;

            if(isWarningsEnabled(player)) {
                // The player has item durability warnings enabled..

                ItemStack hand = player.getInventory().getItemInMainHand();
                ItemMeta itemMeta = hand.getItemMeta();

                if(hand.getType().getMaxDurability() > 0 && itemMeta instanceof Damageable && !isItemWarningsIgnored(hand)) {
                    // The item in the player's hand can have durability, and the item isn't ignored..

                    Damageable damageableMeta = (Damageable) itemMeta;
                    double damageLeft = 1.0 - (double) damageableMeta.getDamage() / (double) hand.getType().getMaxDurability();

                    if(damageLeft < cfg_largeWarningPercent) {
                        // Large warning!
                        hasLargeWarning = true;

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

                        player.sendTitle(
                                Main.instance().getLang().getMessage("large-warning.title", cfg_largeWarningPercent * 100.0),
                                Main.instance().getLang().getMessage("large-warning.sub-title", cfg_largeWarningPercent * 100.0),
                                fadeIn,
                                stay,
                                fadeOut
                        );
                    }else if(damageLeft < cfg_subtleWarningPercent) {
                        // Subtle warning!
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                Main.instance().getLang().getMessage("subtle-warning", cfg_subtleWarningPercent * 100.0)
                        ));
                    }
                }
            }

            if(!hasLargeWarning) {
                playersWithLargeWarning.remove(player);
            }
        });
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
