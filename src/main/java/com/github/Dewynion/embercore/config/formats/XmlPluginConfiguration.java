package com.github.Dewynion.embercore.config.formats;

import com.github.Dewynion.embercore.config.ConfigurationFormat;
import com.github.Dewynion.embercore.config.PluginConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class XmlPluginConfiguration extends PluginConfiguration {
    public XmlPluginConfiguration(File configurationFile, JavaPlugin plugin) {
        super(configurationFile, plugin, ConfigurationFormat.XML);
    }
}
