package dev.blufantasyonline.embercore.config.formats.flatfile;

import dev.blufantasyonline.embercore.config.ConfigurationFormat;
import dev.blufantasyonline.embercore.config.PluginConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class YamlPluginConfiguration extends FlatfilePluginConfiguration {
    public YamlPluginConfiguration(JavaPlugin plugin, File configurationFile) {
        super(plugin, configurationFile, ConfigurationFormat.YAML);
    }
}
