package dev.blufantasyonline.embercore.config;

import dev.blufantasyonline.embercore.config.formats.JsonPluginConfiguration;
import dev.blufantasyonline.embercore.config.formats.XmlPluginConfiguration;
import dev.blufantasyonline.embercore.config.formats.YamlPluginConfiguration;

public enum ConfigurationFormat {
    TEXT("", null),
    YAML(".yml", YamlPluginConfiguration.class),
    XML(".xml", XmlPluginConfiguration.class),
    JSON(".json", JsonPluginConfiguration .class);

    String fileExtension;
    Class<? extends PluginConfiguration> configClass;

    ConfigurationFormat(String fileExtension, Class<? extends PluginConfiguration> configClass) {
        this.fileExtension = fileExtension;
        this.configClass = configClass;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public Class<? extends PluginConfiguration> getConfigClass() {
        return configClass;
    }
}
