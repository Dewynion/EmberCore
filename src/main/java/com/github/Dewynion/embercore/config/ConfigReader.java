package com.github.Dewynion.embercore.config;

import com.github.Dewynion.embercore.CoreLoadPriority;
import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.reflection.Singleton;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

@Singleton(priority = CoreLoadPriority.CONFIG_READER_PRIORITY)
public class ConfigReader {
    public static final String DEFAULT_CONFIG_KEY = "config";
    public static final String DEFAULT_CONFIG_FILENAME = "config.yml";

    private HashMap<JavaPlugin, HashMap<String, FileConfiguration>> pluginConfigs;

    private static ConfigReader instance;

    public static ConfigReader getInstance() {
        return instance;
    }

    public ConfigReader() {
        instance = this;
        pluginConfigs = new HashMap<>();
    }

    /**
     * Use this for first-time registry of your plugin with the config reader.
     * Adds the given plugin to the map with an empty map of config keys to configurations,
     * then adds the default config from config.yml.
     */
    public void registerPlugin(JavaPlugin plugin) {
        EmberCore.log(Level.INFO, "Registering config for " + plugin.getName() + ".");
        pluginConfigs.put(plugin, new HashMap<>());
        if (!new File(plugin.getDataFolder(), DEFAULT_CONFIG_FILENAME).exists())
            plugin.saveDefaultConfig();
        registerConfig(plugin, DEFAULT_CONFIG_KEY, plugin.getConfig());
    }

    public FileConfiguration getConfigFor(JavaPlugin plugin, String configKey) throws
            NullPointerException {
        if (!pluginConfigs.containsKey(plugin)) {
            EmberCore.log(Level.WARNING, "Configs not found for plugin "
                    + plugin.getName() + ". Please register it using " +
                    "ConfigReader::registerPlugin(JavaPlugin).");
            return null;
        }
        return pluginConfigs.get(plugin).get(configKey);
    }

    /**
     * Register a new config for the given plugin under the given key using the
     * specified file.
     *
     * @param plugin     The plugin to register the config for.
     * @param configKey  The key to register the config under.
     *                   Typically the config's name, like "player-data".
     * @param configFile The file to generate a {@link FileConfiguration} from.
     */
    public void registerConfig(JavaPlugin plugin, String configKey, File configFile) {
        if (!configFile.exists()) {
            EmberCore.log(Level.WARNING, plugin.getName() + " attempted to register " +
                    "a config from nonexistent file " + configFile.getPath());
            return;
        }
        FileConfiguration fc = new YamlConfiguration();
        try {
            fc.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        registerConfig(plugin, configKey, fc);
    }

    /**
     * Similar to {@link #registerConfig(JavaPlugin, String, File)}, but accepts an
     * existing {@link FileConfiguration} instead.
     */
    public void registerConfig(JavaPlugin plugin, String configKey, FileConfiguration config) {
        pluginConfigs.get(plugin).put(configKey, config);
        if (configKey.contentEquals(DEFAULT_CONFIG_KEY))
            EmberCore.log(Level.INFO, plugin.getName() + " registered a new default config.");
    }

    /**
     * @param plugin    The plugin to fetch the section for.
     * @param configKey The key of the config containing the desired section.
     * @param path      A string indicating the section's path in config.
     * @return A {@link ConfigurationSection} at path from the requested config.
     */
    public ConfigurationSection getSection(JavaPlugin plugin, String configKey, String path) {
        return pluginConfigs.get(plugin).get(configKey).getConfigurationSection(path);
    }

    /**
     * Attempts to return an object with the same type as the provided default value
     * from the desired config location.
     */
    public Object get(JavaPlugin plugin, String configKey, String path, Object defaultValue) {
        try {
            if (!pluginConfigs.containsKey(plugin)) {
                EmberCore.log(Level.WARNING,
                        "Plugin " + plugin.getName() + " isn't registered with ConfigReader.");
                return defaultValue;
            } else if (!pluginConfigs.get(plugin).containsKey(configKey)) {
                EmberCore.log(Level.WARNING,
                        "Plugin " + plugin.getName() + " doesn't have a key '" +
                        configKey + "'.");
                return defaultValue;
            }
            return pluginConfigs.get(plugin).get(configKey).get(path);
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    public String getString(JavaPlugin plugin, String configKey, String path, String defaultValue) {
        try {
            return get(plugin, configKey, path, defaultValue).toString();
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    public int getInt(JavaPlugin plugin, String configKey, String path, int defaultValue) {
        try {
            return (int) get(plugin, configKey, path, defaultValue);
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    public double getDouble(JavaPlugin plugin, String configKey, String path, double defaultValue) {
        try {
            return (double) get(plugin, configKey, path, defaultValue);
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    public float getFloat(JavaPlugin plugin, String configKey, String path, float defaultValue) {
        try {
            return (float) get(plugin, configKey, path, defaultValue);
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBoolean(JavaPlugin plugin, String configKey, String path, boolean defaultValue) {
        try {
            return (boolean) get(plugin, configKey, path, defaultValue);
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Attempts to return an object of the same type as the default value, located
     * at the given path in the provided ConfigurationSection.
     */
    public static <T> T get(ConfigurationSection section, String path, T defaultValue) {
        try {
            return (T) section.get(path);
        } catch (Exception e) {
            loadErrMsg(e, section.getName(), path, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Sets path to value in the desired config.
     */
    public void set(JavaPlugin plugin, String configKey, String path, Object value) {
        FileConfiguration fc = pluginConfigs.get(plugin).get(configKey);
        fc.set(path, value);
    }

    private static void loadErrMsg(Exception e, JavaPlugin plugin, String configKey,
                                   String path, Object defaultValue) {
        loadErrMsg(e, plugin.getName() + "." + configKey, path, defaultValue);
    }

    private static void loadErrMsg(Exception e, String cfgName, String path, Object defaultValue) {

        EmberCore.log(Level.WARNING, "Exception met when fetching " + path
                + " from config " + cfgName + ": ");
        e.printStackTrace();
        EmberCore.log(Level.INFO, "Using default value " + defaultValue + ".");
    }
}