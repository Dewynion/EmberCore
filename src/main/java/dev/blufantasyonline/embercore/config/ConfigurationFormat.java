package dev.blufantasyonline.embercore.config;

import dev.blufantasyonline.embercore.config.formats.db.MySQLPluginConfiguration;
import dev.blufantasyonline.embercore.config.formats.db.SQLitePluginConfiguration;
import dev.blufantasyonline.embercore.config.formats.flatfile.JsonPluginConfiguration;
import dev.blufantasyonline.embercore.config.formats.flatfile.XmlPluginConfiguration;
import dev.blufantasyonline.embercore.config.formats.flatfile.YamlPluginConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum ConfigurationFormat {
    TEXT(null, false),
    YAML(YamlPluginConfiguration.class, false, "yml", "yaml"),
    XML(XmlPluginConfiguration.class, false, "xml"),
    JSON(JsonPluginConfiguration.class, false, "json"),
    SQLITE(SQLitePluginConfiguration.class, false, "db"),
    SQL(MySQLPluginConfiguration.class, true);

    Set<String> fileExtensions;
    boolean remote;
    Class<? extends PluginConfiguration> configClass;

    ConfigurationFormat(Class<? extends PluginConfiguration> configClass, boolean remote, String... extensions) {
        fileExtensions = new HashSet<>(Arrays.asList(extensions));
        this.remote = remote;
        this.configClass = configClass;
    }

    public Set<String> getFileExtensions() {
        return Collections.unmodifiableSet(fileExtensions);
    }

    public String getFileExtension() {
        return fileExtensions.stream().findFirst().orElse("");
    }

    public static ConfigurationFormat byFileExtension(String extension) {
        extension = extension.replaceFirst(".", "");
        for (ConfigurationFormat fmt : values()) {
            if (fmt.getFileExtensions().contains(extension.toLowerCase()))
                return fmt;
        }
        return TEXT;
    }

    public Class<? extends PluginConfiguration> getConfigClass() {
        return configClass;
    }

    public boolean isRemote() {
        return remote;
    }
}
