package com.github.Dewynion.embercore;

import com.github.Dewynion.embercore.config.ConfigConstants;
import com.github.Dewynion.embercore.config.ConfigReader;
import com.github.Dewynion.embercore.gui.menu.MenuManager;
import com.github.Dewynion.embercore.physics.ProjectileRegistry;
import com.github.Dewynion.embercore.test.command.CommandMenutest;
import com.github.Dewynion.embercore.test.command.CommandShape;
import com.github.Dewynion.embercore.reflection.ReflectionHelper;
import com.github.Dewynion.embercore.test.gui.RandomItemPagedMenu;
import io.netty.handler.logging.LogLevel;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmberCore extends JavaPlugin {
    public static final String CONFIG_KEY = "config.yml";

    private static EmberCore instance;
    private boolean debug = false;

    public static EmberCore getInstance() {
        return instance;
    }

    public static MenuManager getMenuManager() {
        return MenuManager.getInstance();
    }

    public static ConfigReader getConfigReader() {
        return ConfigReader.getInstance();
    }

    public static ProjectileRegistry getProjectileRegistry() {
        return ProjectileRegistry.getInstance();
    }

    /**
     * Shortcut for {@link EmberCore#getInstance()#getLogger()#log(Level, String)}.
     * Less typing.
     */
    public static void log(Level level, String message) {
        instance.getLogger().log(level, message);
    }

    public void onEnable() {
        instance = this;
        ReflectionHelper.registerEvents(this);
        setup(this);
        debug = getConfigReader().getBoolean(this, CONFIG_KEY, ConfigConstants.DEBUG_MODE.getPath(),
                (boolean) ConfigConstants.DEBUG_MODE.getDefaultValue());
        // Only bother with this stuff if we're in debug mode.
        if (debug) {
            new RandomItemPagedMenu();
            registerCommands();
        }
    }

    public void setup(JavaPlugin plugin) {
        ReflectionHelper.registerEvents(plugin);
        getConfigReader().registerPlugin(plugin);
        ReflectionHelper.postSetup(plugin);
    }

    private void registerCommands() {
        getCommand("shape").setExecutor(new CommandShape());
        getCommand("menutest").setExecutor(new CommandMenutest());
    }
}
