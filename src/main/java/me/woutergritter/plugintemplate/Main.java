package me.woutergritter.plugintemplate;

import me.woutergritter.plugintemplate.command.ExampleCMD;
import me.woutergritter.plugintemplate.config.Config;
import me.woutergritter.plugintemplate.config.LangConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance; // Main is a singleton

    // -- Global configuration files -- //
    private Config config;
    private LangConfig langConfig;

    // -- Managers -- //

    @Override
    public void onEnable() {
        instance = this;

        // Load global configs
        config = new Config("config.yml");
        langConfig = new LangConfig("lang.yml");

        // Managers

        // Commands
        new ExampleCMD().register();
    }

    @Override
    public void onDisable() {
        instance = null;
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
