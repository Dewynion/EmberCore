package dev.blufantasyonline.embercore.storage.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.blufantasyonline.embercore.EmberCore;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbConnection {
    public final DbType dbType;
    protected HikariConfig config;
    protected HikariDataSource dataSource;
    private String location;
    private JavaPlugin plugin;

    public DbConnection(JavaPlugin plugin, DbType dbType, String location) {
        this.dbType = dbType;
        this.plugin = plugin;
        this.location = location;
        config = new HikariConfig();
        switch (dbType) {
            case MYSQL -> {
                config.setJdbcUrl(location);
                config.setUsername("database_username");
                config.setPassword("database_password");
            }
            case SQLITE -> {
                config.setPoolName(String.format("%sSQLitePool", plugin.getName()));
                config.setJdbcUrl(String.format("jdbc:sqlite:plugins/%s/%s",
                        plugin.getName(), location));
            }
        }
        config.setConnectionTestQuery("SELECT 1");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        config.setMaximumPoolSize(50);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);
    }

    public ResultSet getAll(String tableName) {
        String query = new StatementBuilder().select().from(tableName).toString();
        try {
            Connection connection = getConnection();
            if (connection == null)
                EmberCore.warn("No connection could be established to %s.", location);
            else {
                PreparedStatement pst = connection.prepareStatement(query);
                return pst.executeQuery();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Connection getConnection() throws NullPointerException {
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            EmberCore.warn("SQL exception: %s", ex);
        }
        return null;
    }
}
