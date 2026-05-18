package com.zetaplugins.lifestealz.storage.connectionPool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zetaplugins.lifestealz.config.StorageConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A MySQL connection pool that manages connections to a MySQL database.
 */
public final class MySQLConnectionPool implements ConnectionPool {
    private final HikariDataSource dataSource;

    /**
     * Constructs a MySQL connection pool with the specified host, port, database, username, and password.
     * @param storageInfo the storage info for the MySQL database
     */
    public MySQLConnectionPool(StorageConfig storageInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + storageInfo.getHost() + ":" + storageInfo.getPort() + "/" + storageInfo.getDatabase());
        config.setUsername(storageInfo.getUsername());
        config.setPassword(storageInfo.getPassword());
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
    }

    /**
     * Returns a connection to the MySQL database from the connection pool.
     * @return a connection to the MySQL database
     * @throws SQLException if an error occurs while getting a connection
     */
    @Override
    public Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new SQLException("Failed to get connection from MySQL connection pool: " + e.getMessage());
        }
    }

    /**
     * Releases a connection back to the MySQL connection pool.
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
     * Shuts down the MySQL connection pool and releases all resources.
     */
    @Override
    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
