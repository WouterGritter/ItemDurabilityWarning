package me.woutergritter.itemdurabilitywarning;

import me.woutergritter.itemdurabilitywarning.command.ItemwarningsCMD;
import me.woutergritter.itemdurabilitywarning.config.Config;
import me.woutergritter.itemdurabilitywarning.config.LangConfig;
import me.woutergritter.itemdurabilitywarning.itemwarning.ItemWarningService;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance; // Main is a singleton

    // -- Global configuration files -- //
    private Config config;
    private LangConfig langConfig;

    // -- Managers -- //
    private ItemWarningService itemWarningService;

    @Override
    public void onEnable() {
        instance = this;

        // Load global configs
        config = new Config("config.yml");
        langConfig = new LangConfig("lang.yml");

        // Managers
        itemWarningService = new ItemWarningService();

        // Commands
        new ItemwarningsCMD().register();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public ItemWarningService getItemWarningService() {
        return itemWarningService;
    }

    public LangConfig getLang() {
        return langConfig;
    }

    // -- Override config methods to use our own implementation -- //
    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void reloadConfig() {
        config.reload();
    }

    @Override
    public void saveConfig() {
        config.save();
    }

    @Override
    public void saveDefaultConfig() {
        config.saveDefault();
    }
    // -- //

    public static Main instance() {
        return instance;
    }
}
