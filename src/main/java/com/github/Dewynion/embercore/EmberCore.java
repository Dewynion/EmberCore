package com.github.Dewynion.embercore;

import com.github.Dewynion.embercore.config.ConfigReader;
import com.github.Dewynion.embercore.gui.menu.MenuManager;
import com.github.Dewynion.embercore.physics.ProjectileRegistry;
import com.github.Dewynion.embercore.test.command.CommandMenutest;
import com.github.Dewynion.embercore.test.command.CommandShape;
import com.github.Dewynion.embercore.reflection.ReflectionHelper;
import io.netty.handler.logging.LogLevel;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmberCore extends JavaPlugin {
    public static final String CONFIG_KEY = "config";

    private static EmberCore instance;

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
        registerCommands();
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
