package com.github.Dewynion.embercore.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class JsonPluginConfiguration extends PluginConfiguration {
    JsonPluginConfiguration(File configurationFile, JavaPlugin plugin ) {
        super(configurationFile, plugin, ConfigurationFormat.JSON);
    }
}
