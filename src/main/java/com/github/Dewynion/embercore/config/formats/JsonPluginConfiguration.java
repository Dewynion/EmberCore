package com.github.Dewynion.embercore.config.formats;

import com.github.Dewynion.embercore.config.ConfigurationFormat;
import com.github.Dewynion.embercore.config.PluginConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class JsonPluginConfiguration extends PluginConfiguration {
    public JsonPluginConfiguration(File configurationFile, JavaPlugin plugin ) {
        super(configurationFile, plugin, ConfigurationFormat.JSON);
    }
}
