package dev.blufantasyonline.embercore.config.formats.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dev.blufantasyonline.embercore.config.ConfigurationFormat;
import dev.blufantasyonline.embercore.config.PluginConfiguration;
import dev.blufantasyonline.embercore.storage.sql.DbConnection;
import dev.blufantasyonline.embercore.storage.sql.DbType;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URISyntaxException;

public class DbPluginConfiguration extends PluginConfiguration {
    public final DbType dbType;
    protected DbConnection connection;

    public DbPluginConfiguration(JavaPlugin plugin, String location, DbType dbType, ConfigurationFormat configurationFormat) throws URISyntaxException {
        super(plugin, location, configurationFormat);
        this.dbType = dbType;
        connection = new DbConnection(plugin, dbType, location);
    }

    @Override
    public JsonNode readConfiguration() {
        return JsonNodeFactory.instance.objectNode();
    }

    @Override
    public void saveConfiguration() {

    }
}
