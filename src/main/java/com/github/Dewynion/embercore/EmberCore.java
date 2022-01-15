package com.github.Dewynion.embercore;

import com.github.Dewynion.embercore.reflection.PluginLoader;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class EmberCore extends JavaPlugin {
    public static final String CONFIG_KEY = "config";

    private Level logLevel = Level.INFO;

    private static EmberCore instance;

    public static EmberCore getInstance() {
        return instance;
    }

    public static void log(Level level, String message, Object... format) {
        log(instance, level, message, format);
    }

    public static void log(JavaPlugin plugin, Level level, String message, Object... format) {
        if (format.length > 0)
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

        // "Noo you can't manually print this"
        printDivider();
        printBlankLine();
        info("  EmberCore v%s", getDescription().getVersion());
        info("  You're running on %s.", getServer().getVersion());
        printBlankLine();
        printDivider();

        String level = getConfig().getString("log-level", "info");
        try {
            logLevel = Level.parse(level.toUpperCase());
        } catch (Exception ignored) {}
        info("Your log level is set to %s.", level.toUpperCase());
        // Level is not an enum, so it's time to pull a YandereDev
        if (logLevel.equals(Level.OFF))
            info("This will disable logging entirely, but will not stop critical errors from printing to console.");
        else if (logLevel.equals(Level.INFO))
            info("This will produce a significant quantity of messages - set your log level to OFF, WARNING or SEVERE to disable this.");

        getLogger().setLevel(logLevel);
        setup(this);
    }

    @Override
    public void onDisable() {
        info("EmberCore v%s is being disabled.", getDescription().getVersion());
    }

    public void setup(JavaPlugin plugin) {
        PluginLoader.register(plugin);
    }

    private void printBlankLine() {
        info("");
    }

    private void printDivider() {
        info("================================================================================================");
    }
}
