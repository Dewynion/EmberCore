package dev.blufantasyonline.embercore.config.formats.db;

import dev.blufantasyonline.embercore.config.ConfigurationFormat;
import dev.blufantasyonline.embercore.storage.sql.DbType;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;

public final class MySQLPluginConfiguration extends DbPluginConfiguration {
    public MySQLPluginConfiguration(JavaPlugin plugin, String location) throws URISyntaxException {
        super(plugin, location, DbType.SQLITE, ConfigurationFormat.SQL);
    }
}
