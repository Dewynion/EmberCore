package com.github.dewyn.embercore;

import com.github.dewyn.embercore.command.*;
import com.github.dewyn.embercore.config.ConfigConstants;
import com.github.dewyn.embercore.reflection.ReflectionHelper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class EmberCore extends JavaPlugin {
    private static EmberCore instance;

    public static EmberCore getInstance() {
        return instance;
    }

    public boolean debug = false;

    public void onEnable() {
        instance = this;
        ReflectionHelper.loadSingletons(this);
        ReflectionHelper.registerEvents(this);

        File config = new File(getDataFolder(), "config.yml");
        if (!config.exists())
            saveDefaultConfig();
        debug = getConfig().getBoolean(ConfigConstants.DEBUG.getPath());

        registerCommands();
    }

    private void registerCommands() {
        getCommand("shape").setExecutor(new CommandShape());
    }
}
