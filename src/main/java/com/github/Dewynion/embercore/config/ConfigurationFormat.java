package com.github.Dewynion.embercore.config;

public enum ConfigurationFormat {
    TEXT("", null),
    YAML(".yml", YamlPluginConfiguration.class),
    JSON(".json", JsonPluginConfiguration.class);

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
