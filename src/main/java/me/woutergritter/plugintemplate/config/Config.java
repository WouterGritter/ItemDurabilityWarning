package me.woutergritter.plugintemplate.config;

import me.woutergritter.plugintemplate.Main;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class Config extends YamlConfiguration {
    private final String name;
    private final File file;

    public Config(String name) {
        this.name = name;
        this.file = new File(Main.instance().getDataFolder(), name);

        saveDefault();
        reload();
    }

    public boolean hasDefaults() {
        return Main.instance().getResource(name) != null;
    }

    public void reload() {
        if(file.exists()) {
            try {
                load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

        if(hasDefaults()) {
            InputStreamReader defConfigStream = new InputStreamReader(Objects.requireNonNull(Main.instance().getResource(name)));
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);

            setDefaults(defConfig);
        }
    }

    public void save() {
        try {
            file.getParentFile().mkdirs();

            save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefault() {
        if(!file.exists()) {
            file.getParentFile().mkdirs();

            if(hasDefaults()) {
                Main.instance().saveResource(name, false);
            }else{
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getName() {
        return name;
    }
}
