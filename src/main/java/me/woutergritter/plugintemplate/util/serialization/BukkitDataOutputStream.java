package me.woutergritter.plugintemplate.util.serialization;

import me.woutergritter.plugintemplate.util.function.ThrowingBiConsumer;
import me.woutergritter.plugintemplate.util.function.ThrowingConsumer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class BukkitDataOutputStream extends DataOutputStream {
    public BukkitDataOutputStream(OutputStream out) {
        super(out);
    }

    public void writeLocation(Location location) throws IOException {
        writeUTF(location.getWorld().getName());

        writeDouble(location.getX());
        writeDouble(location.getY());
        writeDouble(location.getZ());

        writeFloat(location.getYaw());
        writeFloat(location.getPitch());
    }

    public void writeBlock(Block block) throws IOException {
        writeUTF(block.getWorld().getName());

        writeInt(block.getX());
        writeInt(block.getY());
        writeInt(block.getZ());
    }

    public void writeUUID(UUID uuid) throws IOException {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public void writeEnum(Enum<?> e) throws IOException {
        writeUTF(e.name());
    }

    public void writeItemStackYaml(ItemStack itemStack) throws IOException {
        // Hacky way of writing an ItemStack, I know..
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("v", itemStack);

        writeUTF(yaml.saveToString());
    }

    // -- Lists and maps -- //

    public <T> void writeList(Collection<T> list, ThrowingBiConsumer<T, BukkitDataOutputStream, IOException> serializeFunction) throws IOException {
        writeInt(list != null ? list.size() : 0);

        if(list != null) {
            for (T element : list) {
                serializeFunction.accept(element, this);
            }
        }
    }

    public <T> void writeList(Collection<T> list, ThrowingConsumer<T, IOException> serializeFunction) throws IOException {
        writeList(list, (element, _dos) -> serializeFunction.accept(element));
    }

    public <T, U> void writeMap(Map<T, U> map, ThrowingBiConsumer<T, BukkitDataOutputStream, IOException> keySerializeFunction,
                                ThrowingBiConsumer<U, BukkitDataOutputStream, IOException> valueSerializeFunction) throws IOException {
        writeInt(map != null ? map.size() : 0);

        if(map != null) {
            for(T key : map.keySet()) {
                U value = map.get(key);

                keySerializeFunction.accept(key, this);
                valueSerializeFunction.accept(value, this);
            }
        }
    }

    public <T, U> void writeMap(Map<T, U> map, ThrowingBiConsumer<T, BukkitDataOutputStream, IOException> keySerializeFunction,
                                ThrowingConsumer<U, IOException> valueSerializeFunction) throws IOException {
        writeMap(map, keySerializeFunction, (value, _dos) -> valueSerializeFunction.accept(value));
    }

    public <T, U> void writeMap(Map<T, U> map, ThrowingConsumer<T, IOException> keySerializeFunction,
                                ThrowingBiConsumer<U, BukkitDataOutputStream, IOException> valueSerializeFunction) throws IOException {
        writeMap(map, (key, _dos) -> keySerializeFunction.accept(key), valueSerializeFunction);
    }

    public <T, U> void writeMap(Map<T, U> map, ThrowingConsumer<T, IOException> keySerializeFunction,
                                ThrowingConsumer<U, IOException> valueSerializeFunction) throws IOException {
        writeMap(map, (key, _dos) -> keySerializeFunction.accept(key), (value, _dos) -> valueSerializeFunction.accept(value));
    }
}
