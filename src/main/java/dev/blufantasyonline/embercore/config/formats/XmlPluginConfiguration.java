package dev.blufantasyonline.embercore.config.formats;

import dev.blufantasyonline.embercore.config.ConfigurationFormat;
import dev.blufantasyonline.embercore.config.PluginConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class XmlPluginConfiguration extends PluginConfiguration {
    public XmlPluginConfiguration(File configurationFile, JavaPlugin plugin) {
        super(configurationFile, plugin, ConfigurationFormat.XML);
    }
}
