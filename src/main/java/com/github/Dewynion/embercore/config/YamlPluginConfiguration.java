package com.github.Dewynion.embercore.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class YamlPluginConfiguration extends PluginConfiguration {
    YamlPluginConfiguration(File configurationFile, JavaPlugin plugin) {
        super(configurationFile, plugin, ConfigurationFormat.YAML);
    }
}
