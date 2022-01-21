package dev.blufantasyonline.embercore.config.serialization;

import org.bukkit.configuration.ConfigurationSection;

public interface TypeSerializer<T> {
    ConfigurationSection serialize(T object);
    T deserialize(ConfigurationSection configurationSection);
    T fromString(String string);
    String toString(T object);
}
