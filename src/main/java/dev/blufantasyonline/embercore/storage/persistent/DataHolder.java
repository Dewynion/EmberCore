package dev.blufantasyonline.embercore.storage.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class DataHolder {
    protected final JavaPlugin plugin;
    protected final PersistentDataHolder spigotDataHolder;
    protected final PersistentDataContainer container;

    public DataHolder(JavaPlugin plugin, PersistentDataHolder spigotDataHolder) {
        this.plugin = plugin;
        this.spigotDataHolder = spigotDataHolder;
        this.container = spigotDataHolder.getPersistentDataContainer();
    }

    public final <T, Z> Z get(String key, PersistentDataType<T, Z> dataType) {
        NamespacedKey k = key(key);
        if (container.has(k, dataType))
            return container.get(k, dataType);
        return null;
    }

    public final <T, Z> Z get(String key, PersistentDataType<T, Z> dataType, Z defaultValue) {
        return get(key(key), dataType, defaultValue);
    }

    public final <T, Z> Z get(NamespacedKey key, PersistentDataType<T, Z> dataType, Z defaultValue) {
        return container.getOrDefault(key, dataType, defaultValue);
    }

    public final <T, Z> void set(String key, PersistentDataType<T, Z> dataType, Z value) {
        set(key(key), dataType, value);
    }

    public final <T, Z> void set(NamespacedKey key, PersistentDataType<T, Z> dataType, Z value) {
        container.set(key, dataType, value);
        if (spigotDataHolder instanceof TileState)
            ((TileState) spigotDataHolder).update();
    }

    public final NamespacedKey key(String... key) {
        StringBuilder sb = new StringBuilder();
        for (String s : key)
            sb.append(s).append(".");
        return new NamespacedKey(plugin, sb.substring(0, sb.length() - 1));
    }
}
