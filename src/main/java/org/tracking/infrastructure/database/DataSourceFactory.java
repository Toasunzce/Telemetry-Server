package org.tracking.infrastructure.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class DataSourceFactory {
    private DataSourceFactory() {}

    public static DataSource create() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env("DB_URL", "jdbc:postgresql://localhost:5432/tracking"));
        config.setUsername(env("DB_USER", "admin"));
        config.setPassword(env("DB_PASSWORD", "12345678"));
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null ? value : defaultValue;
    }
}