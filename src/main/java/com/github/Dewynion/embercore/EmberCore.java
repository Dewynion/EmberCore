package com.github.Dewynion.embercore;

import com.github.Dewynion.embercore.config.ConfigManager;
import com.github.Dewynion.embercore.physics.ProjectileRegistry;
import com.github.Dewynion.embercore.reflection.ReflectionHelper;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class EmberCore extends JavaPlugin {
    public static final String CONFIG_KEY = "config";

    private static EmberCore instance;

    public static EmberCore getInstance() {
        return instance;
    }

    public static ConfigManager getConfigManager() {
        return ConfigManager.getInstance();
    }

    public static ProjectileRegistry getProjectileRegistry() {
        return ProjectileRegistry.getInstance();
    }

    public static void log(Level level, String message, Object... format) {
        log(instance, level, message, format);
    }

    public static void log(JavaPlugin plugin, Level level, String message, Object... format) {
        if (format.length != 0)
            message = String.format(message, format);
        plugin.getLogger().log(level, message);
    }

    public static void info(JavaPlugin plugin, String message, Object... format) {
        log(plugin, Level.INFO, message, format);
    }

    public static void info(String message, Object... format) {
        info(instance, message, format);
    }

    public static void warn(JavaPlugin plugin, String message, Object... format) {
        log(plugin, Level.WARNING, message, format);
    }

    public static void warn(String message, Object... format) {
        warn(instance, message, format);
    }

    public static void severe(JavaPlugin plugin, String message, Object... format) {
        log(plugin, Level.SEVERE, message, format);
    }

    public static void severe(String message, Object... format) {
        severe(instance, message, format);
    }

    public void onEnable() {
        instance = this;
        ReflectionHelper.registerEvents(this);
        setup(this);
    }

    public void setup(JavaPlugin plugin) {
        ReflectionHelper.registerEvents(plugin);
        getConfigManager().registerPlugin(plugin);
        ReflectionHelper.postSetup(plugin);
    }
}
