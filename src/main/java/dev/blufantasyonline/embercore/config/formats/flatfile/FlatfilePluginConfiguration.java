package dev.blufantasyonline.embercore.config.formats.flatfile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import dev.blufantasyonline.embercore.config.ConfigurationFormat;
import dev.blufantasyonline.embercore.config.PluginConfiguration;
import dev.blufantasyonline.embercore.reflection.ConfigInjector;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public abstract class FlatfilePluginConfiguration extends PluginConfiguration {
    protected final File configurationFile;

    public FlatfilePluginConfiguration(JavaPlugin plugin, File configurationFile, ConfigurationFormat configurationFormat) {
        super(plugin, configurationFile.getAbsolutePath(), configurationFormat);
        this.configurationFile = configurationFile;
    }

    @Override
    public void saveConfiguration() {
        try {
            ConfigInjector.getObjectMapper(plugin, configurationFormat).writeValue(configurationFile, configRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonNode readConfiguration() {
        try {
            JsonNode root = ConfigInjector.getObjectMapper(plugin, configurationFormat).readTree(configurationFile);
            if (root == null || root instanceof NullNode || root instanceof MissingNode)
                return JsonNodeFactory.instance.objectNode();
            return root;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return JsonNodeFactory.instance.objectNode();
    }

    public final File getConfigurationFile() {
        return configurationFile;
    }
}
