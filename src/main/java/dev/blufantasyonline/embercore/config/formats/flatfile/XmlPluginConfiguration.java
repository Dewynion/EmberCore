package dev.blufantasyonline.embercore.config.formats.flatfile;

import dev.blufantasyonline.embercore.config.ConfigurationFormat;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class XmlPluginConfiguration extends FlatfilePluginConfiguration {
    public XmlPluginConfiguration(JavaPlugin plugin, File configurationFile) {
        super(plugin, configurationFile, ConfigurationFormat.XML);
    }
}
