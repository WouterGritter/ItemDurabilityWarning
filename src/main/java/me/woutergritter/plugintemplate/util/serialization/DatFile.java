package me.woutergritter.plugintemplate.util.serialization;

import me.woutergritter.plugintemplate.Main;
import me.woutergritter.plugintemplate.util.function.ThrowingConsumer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DatFile {
    private final File file;
    private final ThrowingConsumer<BukkitDataInputStream, IOException> loader;
    private final ThrowingConsumer<BukkitDataOutputStream, IOException> saver;

    private boolean saveScheduled = false;

    public DatFile(File file, ThrowingConsumer<BukkitDataInputStream, IOException> loader, ThrowingConsumer<BukkitDataOutputStream, IOException> saver) {
        this.file = file;
        this.loader = loader;
        this.saver = saver;
    }

    public DatFile savePeriodically(int intervalTicks, boolean async) {
        if(async) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(Main.instance(), this::saveIfScheduled, intervalTicks, intervalTicks);
        }else{
            Bukkit.getScheduler().runTaskTimer(Main.instance(), this::saveIfScheduled, intervalTicks, intervalTicks);
        }

        return this;
    }

    public DatFile savePeriodicallyMinute(boolean async) {
        savePeriodically(20 * 60, async);

        return this;
    }

    public DatFile saveOnDisable() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPluginDisableEvent(PluginDisableEvent e) {
                if(e.getPlugin() == Main.instance()) {
                    saveIfScheduled();
                }
            }
        }, Main.instance());

        return this;
    }

    public DatFile saveIfScheduled() {
        if(saveScheduled) {
            save(true);
            saveScheduled = false;
        }

        return this;
    }

    public DatFile save() {
        save(false);

        return this;
    }

    public DatFile save(boolean force) {
        if(!force) {
            saveScheduled = true;
            return this;
        }

        BukkitDataOutputStream dos = null;
        try{
            dos = new BukkitDataOutputStream(new FileOutputStream(file));
            saver.accept(dos);
        }catch(IOException e) {
            Main.instance().getLogger().warning("Could not save dat file '" + file.getName() + "': " + e.toString());
            e.printStackTrace();
        }

        if(dos != null) {
            try{
                dos.close();
            }catch(IOException ignored) {}
        }

        saveScheduled = false;

        return this;
    }

    public DatFile load() {
        if(!file.exists()) {
            save(true);
        }

        BukkitDataInputStream dis = null;
        try{
            dis = new BukkitDataInputStream(new FileInputStream(file));
            loader.accept(dis);
        }catch(IOException e) {
            Main.instance().getLogger().warning("Could not load dat file '" + file.getName() + "': " + e.toString());
            e.printStackTrace();
        }

        if(dis != null) {
            try{
                dis.close();
            }catch(IOException ignored) {}
        }

        return this;
    }

    public boolean isSaveScheduled() {
        return saveScheduled;
    }
}
