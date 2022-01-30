package dev.blufantasyonline.embercore;

import dev.blufantasyonline.embercore.reflection.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class EmberCore extends JavaPlugin {
    public static final String CONFIG_KEY = "config";

    public static int serverTickrate = 20;

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

    public static void logSerialization(JavaPlugin plugin, String message, Object... format) {
        if (CoreSettings.coreLogSettings.serialization)
            info(plugin, message, format);
    }

    public static void logSerialization(String message, Object... format) {
        if (CoreSettings.coreLogSettings.serialization)
            info(message, format);
    }

    public static void logInjection(JavaPlugin plugin, String message, Object... format) {
        if (CoreSettings.coreLogSettings.injection)
            info(plugin, message, format);
    }

    public static void logInjection(String message, Object... format) {
        if (CoreSettings.coreLogSettings.injection)
            info(message, format);
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
