package dev.blufantasyonline.embercore.config.formats;

import dev.blufantasyonline.embercore.config.ConfigurationFormat;
import dev.blufantasyonline.embercore.config.PluginConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class JsonPluginConfiguration extends PluginConfiguration {
    public JsonPluginConfiguration(File configurationFile, JavaPlugin plugin ) {
        super(configurationFile, plugin, ConfigurationFormat.JSON);
    }
}
