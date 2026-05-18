package com.zetaplugins.lifestealz.storage.connectionPool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zetaplugins.lifestealz.config.StorageConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A MariaDB connection pool that manages connections to a MariaDB database.
 */
public final class MariaDBConnectionPool implements ConnectionPool {
    private final HikariDataSource dataSource;

    /**
     * Constructs a MariaDB connection pool with the specified host, port, database, username, and password.
     * @param storageInfo the storage info for the MariaDB database
     */
    public MariaDBConnectionPool(StorageConfig storageInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://" + storageInfo.getHost() + ":" + storageInfo.getPort() + "/" + storageInfo.getDatabase());
        config.setUsername(storageInfo.getUsername());
        config.setPassword(storageInfo.getPassword());
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
    }

    /**
     * Returns a connection to the MariaDB database from the connection pool.
     * @return a connection to the MariaDB database
     * @throws SQLException if an error occurs while getting a connection
     */
    @Override
    public Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new SQLException("Failed to get connection from MariaDB connection pool: " + e.getMessage());
        }
    }

    /**
     * Releases a connection back to the MariaDB connection pool.
     * @param connection the connection to release
     * @throws SQLException if an error occurs while releasing the connection
     */
    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Shuts down the MariaDB connection pool and releases all resources.
     */
    @Override
    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
