package com.github.dewyn.embercore;

import com.github.dewyn.embercore.test.command.CommandMenutest;
import com.github.dewyn.embercore.test.command.CommandShape;
import com.github.dewyn.embercore.reflection.ReflectionHelper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class EmberCore extends JavaPlugin {
    private static EmberCore instance;

    public static EmberCore getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        ReflectionHelper.getSingletons(this);
        ReflectionHelper.registerEvents(this);

        File config = new File(getDataFolder(), "config.yml");
        if (!config.exists())
            saveDefaultConfig();
        registerCommands();
    }

    private void registerCommands() {
        getCommand("shape").setExecutor(new CommandShape());
        getCommand("menutest").setExecutor(new CommandMenutest());
    }
}
