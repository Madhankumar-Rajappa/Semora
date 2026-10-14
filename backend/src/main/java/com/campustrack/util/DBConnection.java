package com.campustrack.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection provider using HikariCP connection pooling
 * with environment variable support for cloud/container deployment.
 */
public class DBConnection {

    private static HikariDataSource dataSource;
    private static boolean poolInitialized = false;

    // Default connection parameters
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "campustrack";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";

    static {
        initPool();
    }

    private static synchronized void initPool() {
        if (poolInitialized && dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Read environment variables (supports Railway, Render, PlanetScale, Aiven, Docker, Eclipse)
            String envUrl = System.getenv("DB_URL");
            String envHost = System.getenv("DB_HOST");
            String envPort = System.getenv("DB_PORT");
            String envName = System.getenv("DB_NAME");
            String envUser = System.getenv("DB_USER");
            String envPass = System.getenv("DB_PASSWORD");

            String jdbcUrl;
            if (envUrl != null && !envUrl.trim().isEmpty()) {
                jdbcUrl = envUrl.trim();
            } else {
                String host = (envHost != null && !envHost.trim().isEmpty()) ? envHost : DEFAULT_HOST;
                String port = (envPort != null && !envPort.trim().isEmpty()) ? envPort : DEFAULT_PORT;
                String dbName = (envName != null && !envName.trim().isEmpty()) ? envName : DEFAULT_DB;
                jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName 
                        + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false";
            }

            String user = (envUser != null) ? envUser : DEFAULT_USER;
            String pass = (envPass != null) ? envPass : DEFAULT_PASS;

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(user);
            config.setPassword(pass);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setMaxLifetime(600000);
            config.setConnectionTimeout(10000);
            config.setPoolName("CampusTrackHikariPool");

            // Recommended MySQL performance parameters
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            poolInitialized = true;
            System.out.println("[DBConnection] HikariCP pool initialized successfully with URL: " + jdbcUrl);

        } catch (Throwable e) {
            System.err.println("[DBConnection] Warning: Hikari pool init failed (" + e.getMessage() + "). Will use fallback DriverManager on demand.");
            poolInitialized = false;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (poolInitialized && dataSource != null && !dataSource.isClosed()) {
            try {
                return dataSource.getConnection();
            } catch (SQLException ex) {
                System.err.println("[DBConnection] Pool getConnection error, trying fallback: " + ex.getMessage());
            }
        }

        // Direct fallback
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String envUrl = System.getenv("DB_URL");
            String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : DEFAULT_HOST;
            String port = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : DEFAULT_PORT;
            String db = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : DEFAULT_DB;
            String url = envUrl != null ? envUrl : "jdbc:mysql://" + host + ":" + port + "/" + db + "?serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false";
            String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DEFAULT_USER;
            String pass = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DEFAULT_PASS;
            return DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found: " + e.getMessage(), e);
        }
    }

    public static boolean checkConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            poolInitialized = false;
            System.out.println("[DBConnection] HikariCP pool closed.");
        }
    }
}
