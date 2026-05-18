package com.zetaplugins.lifestealz.storage;

import com.zetaplugins.lifestealz.LifeStealZ;
import com.zetaplugins.lifestealz.storage.connectionPool.ConnectionPool;
import com.zetaplugins.lifestealz.storage.connectionPool.MariaDBConnectionPool;

import java.sql.*;

/**
 * Storage class for MariaDB.
 */
public final class MariaDBStorage extends MySQLSyntaxStorage {
    private final MariaDBConnectionPool connectionPool;

    public MariaDBStorage(LifeStealZ plugin) {
        super(plugin);

        connectionPool = new MariaDBConnectionPool(plugin.getConfig().getStorage());
    }

    @Override
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    @Override
    protected void migrateDatabase() {
        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'hearts' AND COLUMN_NAME = 'firstJoin'")
        ) {
            if (!resultSet.next()) {
                LifeStealZ.LOGGER.info("Adding 'firstJoin' column to 'hearts' table.");
                statement.executeUpdate("ALTER TABLE hearts ADD COLUMN firstJoin INTEGER DEFAULT 0");
            }
        } catch (SQLException e) {
            LifeStealZ.LOGGER.error("Failed to migrate database: ", e);
        }
    }
}
