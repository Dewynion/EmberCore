package dev.blufantasyonline.embercore.storage.sql;

public final class DbTable {
    protected DbConnection connection;
    private String name;

    public DbTable(DbConnection dbConnection, String name) {
        this.connection = dbConnection;
        this.name = name;
    }


}
