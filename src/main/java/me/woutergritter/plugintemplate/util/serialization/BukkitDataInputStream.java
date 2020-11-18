package me.woutergritter.plugintemplate.util.serialization;

import me.woutergritter.plugintemplate.util.function.ThrowingFunction;
import me.woutergritter.plugintemplate.util.function.ThrowingSupplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class BukkitDataInputStream extends DataInputStream {
    public BukkitDataInputStream(InputStream in) {
        super(in);
    }

    public Location readLocation() throws IOException {
        World world = Bukkit.getWorld(readUTF());

        double x = readDouble();
        double y = readDouble();
        double z = readDouble();

        float yaw = readFloat();
        float pitch = readFloat();

        if(world == null) {
            return null;
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    public Block readBlock() throws IOException {
        World world = Bukkit.getWorld(readUTF());

        int x = readInt();
        int y = readInt();
        int z = readInt();

        if(world == null) {
            return null;
        }

        return world.getBlockAt(x, y, z);
    }

    public UUID readUUID() throws IOException {
        long mostSigBits = readLong();
        long leastSigBits = readLong();

        return new UUID(mostSigBits, leastSigBits);
    }

    public <T extends Enum<T>> T readEnum(Class<T> enumType) throws IOException {
        return Enum.valueOf(enumType, readUTF());
    }

    public ItemStack readItemStackYaml() throws IOException {
        // Hacky way of writing an ItemStack, I know..
        String yamlStr = readUTF();

        YamlConfiguration yaml = new YamlConfiguration();
        try{
            yaml.loadFromString(yamlStr);
        }catch(InvalidConfigurationException ex) {
            return null;
        }

        return yaml.getItemStack("v");
    }

    // -- Lists and maps -- //

    public <T> List<T> readList(ThrowingFunction<BukkitDataInputStream, T, IOException> deserializeFunction) throws IOException {
        int size = readInt();
        List<T> list = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            T element = deserializeFunction.apply(this);
            if (element != null) {
                list.add(element);
            }
        }

        return list;
    }

    public <T> List<T> readList(ThrowingSupplier<T, IOException> deserializeFunction) throws IOException {
        return readList(_dis -> deserializeFunction.get());
    }

    public <T, U> Map<T, U> readMap(ThrowingFunction<BukkitDataInputStream, T, IOException> keyDeserializeFunction,
                                    ThrowingFunction<BukkitDataInputStream, U, IOException> valueDeserializeFunction) throws IOException {
        int size = readInt();
        Map<T, U> map = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            T key = keyDeserializeFunction.apply(this);
            U value = valueDeserializeFunction.apply(this);

            if (key != null && value != null) {
                map.put(key, value);
            }
        }

        return map;
    }

    public <T, U> Map<T, U> readMap(ThrowingFunction<BukkitDataInputStream, T, IOException> keyDeserializeFunction,
                                    ThrowingSupplier<U, IOException> valueDeserializeFunction) throws IOException {
        return readMap(keyDeserializeFunction, _dis -> valueDeserializeFunction.get());
    }

    public <T, U> Map<T, U> readMap(ThrowingSupplier<T, IOException> keyDeserializeFunction,
                                    ThrowingFunction<BukkitDataInputStream, U, IOException> valueDeserializeFunction) throws IOException {
        return readMap(_dis -> keyDeserializeFunction.get(), valueDeserializeFunction);
    }

    public <T, U> Map<T, U> readMap(ThrowingSupplier<T, IOException> keyDeserializeFunction,
                                    ThrowingSupplier<U, IOException> valueDeserializeFunction) throws IOException {
        return readMap(_dis -> keyDeserializeFunction.get(), _dis -> valueDeserializeFunction.get());
    }
}
