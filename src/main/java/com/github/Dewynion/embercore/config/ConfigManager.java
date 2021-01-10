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

@Singleton(priority = CoreLoadPriority.CONFIG_MANAGER_PRIORITY)
public class ConfigManager {
    public static final String DEFAULT_CONFIG_KEY = "config";
    public static final String DEFAULT_CONFIG_FILENAME = "config.yml";

    private HashMap<JavaPlugin, HashMap<String, FileConfigPair>> pluginConfigs;

    private static ConfigManager instance;

    public static ConfigManager getInstance() {
        return instance;
    }

    public ConfigManager() {
        instance = this;
        pluginConfigs = new HashMap<>();
    }

    /**
     * Use this for first-time registry of your plugin with the config manager.
     * Adds the given plugin to the map with an empty map of config keys to configurations,
     * then adds the default config from config.yml.
     */
    public void registerPlugin(JavaPlugin plugin) {
        EmberCore.log(Level.INFO, "Registering config for " + plugin.getName() + ".");
        pluginConfigs.put(plugin, new HashMap<>());
        File defaultConfigFile = new File(plugin.getDataFolder(), DEFAULT_CONFIG_FILENAME);
        if (!defaultConfigFile.exists())
            plugin.saveDefaultConfig();
        registerConfig(plugin, DEFAULT_CONFIG_KEY, defaultConfigFile, plugin.getConfig());
    }

    public FileConfiguration getDefaultConfig(JavaPlugin plugin) throws NullPointerException {
        if (!pluginConfigs.containsKey(plugin)) {
            EmberCore.log(Level.WARNING, "Configs not found for plugin "
                    + plugin.getName() + ". Please register it using " +
                    "ConfigReader::registerPlugin(JavaPlugin).");
            return null;
        }
        return pluginConfigs.get(plugin).get(DEFAULT_CONFIG_KEY).config;
    }

    public FileConfiguration getConfigFor(JavaPlugin plugin, String configKey) throws
            NullPointerException {
        if (!pluginConfigs.containsKey(plugin)) {
            EmberCore.log(Level.WARNING, "Configs not found for plugin "
                    + plugin.getName() + ". Please register it using " +
                    "ConfigManager::registerPlugin(JavaPlugin).");
            return null;
        }
        return pluginConfigs.get(plugin).get(configKey).config;
    }

    public void saveConfig(JavaPlugin plugin, String configKey) {
        if (!pluginConfigs.containsKey(plugin)) {
            EmberCore.log(Level.WARNING, "Configs not found for plugin "
                    + plugin.getName() + ". Please register it using " +
                    "ConfigReader::registerPlugin(JavaPlugin).");
        } else {
            FileConfigPair pair = pluginConfigs.get(plugin).get(configKey);
            try {
                pair.config.save(pair.file);
            } catch (IOException ex) {
                EmberCore.log(Level.WARNING,
                        String.format("Unable to save config '%s' for plugin %s. Couldn't access config file %s.",
                                configKey, plugin.getName(), pair.file));
            }
        }
    }

    public void saveDefaultConfig(JavaPlugin plugin) {
        saveConfig(plugin, DEFAULT_CONFIG_KEY);
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
    public void registerConfig(JavaPlugin plugin, String configKey, File configFile, boolean createFile) {
        if (!configFile.exists()) {
            if (!createFile) {
                EmberCore.log(Level.WARNING, plugin.getName() + " attempted to register "
                        + "a config from nonexistent file " + configFile.getPath()
                        + " without allowing new file creation.");
                return;
            }
            try {
                configFile.createNewFile();
            } catch (IOException ex) {
                EmberCore.log(Level.SEVERE, "I/O error: Unable to create config file "
                        + configFile.getPath() + " for plugin " + plugin.getName() + ".");
                return;
            }
        }
        FileConfiguration fc = new YamlConfiguration();
        try {
            fc.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        registerConfig(plugin, configKey, configFile, fc);
    }

    /**
     * Similar to {@link #registerConfig(JavaPlugin, String, File, boolean)}, but accepts an
     * existing {@link FileConfiguration} instead.
     */
    public void registerConfig(JavaPlugin plugin, String configKey, File file, FileConfiguration config) {
        pluginConfigs.get(plugin).put(configKey, new FileConfigPair(file, config));
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
        return pluginConfigs.get(plugin).get(configKey).config.getConfigurationSection(path);
    }

    /**
     * Attempts to return an object with the same type as the provided default value
     * from the desired config location.
     */
    public Object get(JavaPlugin plugin, String configKey, String path, Object defaultValue, boolean setIfNotExists) {
        try {
            if (!pluginConfigs.containsKey(plugin)) {
                EmberCore.log(Level.WARNING,
                        "Plugin " + plugin.getName() + " isn't registered with ConfigManager.");
                return defaultValue;
            } else if (!pluginConfigs.get(plugin).containsKey(configKey)) {
                EmberCore.log(Level.WARNING,
                        "Plugin " + plugin.getName() + " doesn't have a key '" +
                                configKey + "'.");
                return defaultValue;
            }
            return pluginConfigs.get(plugin).get(configKey).config.get(path);
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            if (setIfNotExists)
                set(plugin, configKey, path, defaultValue);
        }
        return defaultValue;
    }

    public String getString(JavaPlugin plugin, String configKey, String path, String defaultValue) {
        return getString(plugin, configKey, path, defaultValue, false);
    }

    public String getString(JavaPlugin plugin, String configKey, String path, String defaultValue, boolean setIfNotExists) {
        try {
            return get(plugin, configKey, path, defaultValue, setIfNotExists).toString();
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    public int getInt(JavaPlugin plugin, String configKey, String path, int defaultValue) {
        return getInt(plugin, configKey, path, defaultValue, false);
    }

    public int getInt(JavaPlugin plugin, String configKey, String path, int defaultValue, boolean setIfNotExists) {
        try {
            return (int) get(plugin, configKey, path, defaultValue, setIfNotExists);
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    public double getDouble(JavaPlugin plugin, String configKey, String path, double defaultValue) {
        return getDouble(plugin, configKey, path, defaultValue, false);
    }

    public double getDouble(JavaPlugin plugin, String configKey, String path, double defaultValue, boolean setIfNotExists) {
        try {
            return (double) get(plugin, configKey, path, defaultValue, setIfNotExists);
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    public float getFloat(JavaPlugin plugin, String configKey, String path, float defaultValue) {
        return getFloat(plugin, configKey, path, defaultValue, false);
    }

    public float getFloat(JavaPlugin plugin, String configKey, String path, float defaultValue, boolean setIfNotExists) {
        try {
            return (float) get(plugin, configKey, path, defaultValue, setIfNotExists);
        } catch (Exception e) {
            loadErrMsg(e, plugin, configKey, path, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBoolean(JavaPlugin plugin, String configKey, String path, boolean defaultValue) {
        return getBoolean(plugin, configKey, path, defaultValue, false);
    }

    public boolean getBoolean(JavaPlugin plugin, String configKey, String path, boolean defaultValue, boolean setIfNotExists) {
        try {
            return (boolean) get(plugin, configKey, path, defaultValue, setIfNotExists);
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
     * Static set that only requires a configuration section. Not really necessary but does log
     * errors so that's cool.
     */
    public static void set(ConfigurationSection section, String path, Object value) {
        try {
            section.set(path, value);
        } catch (Exception e) {
            EmberCore.log(Level.WARNING, "Unable to set value " + value.toString()
                    + " for path " + path + ".");
            e.printStackTrace();
        }
    }

    /**
     * Sets path to value in the desired config.
     */
    public void set(JavaPlugin plugin, String configKey, String path, Object value) {
        FileConfigPair fcp = pluginConfigs.get(plugin).get(configKey);
        fcp.config.set(path, value);
        saveConfig(plugin, configKey);
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

    private class FileConfigPair {
        private final FileConfiguration config;
        private final File file;

        private FileConfigPair(File f, FileConfiguration fc) {
            config = fc;
            file = f;
        }
    }
}
