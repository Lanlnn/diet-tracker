package com.diettracker.tools;

import org.flywaydb.core.Flyway;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

public final class LegacyMysqlFlywayAdopter {
    private LegacyMysqlFlywayAdopter() {}

    public static void main(String[] args) throws Exception {
        String url = required("DB_URL");
        String username = required("DB_USERNAME");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        String confirmedDatabase = required("MIGRATION_CONFIRM_DATABASE");
        String database = databaseName(url);
        if (!database.equals(confirmedDatabase)) {
            throw new IllegalArgumentException("MIGRATION_CONFIRM_DATABASE does not match JDBC database");
        }

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            DatabaseMetaData metadata = connection.getMetaData();
            if (!metadata.getDatabaseProductName().toLowerCase().contains("mysql")
                    || metadata.getDatabaseMajorVersion() < 8) {
                throw new IllegalStateException("Legacy adoption only supports MySQL 8 or newer");
            }
        }

        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .baselineOnMigrate(true)
                .baselineVersion("5")
                .baselineDescription("Legacy MySQL 8 schema adopted at V5")
                .validateMigrationNaming(true)
                .load();
        flyway.migrate();
        flyway.validate();
        System.out.println("Legacy MySQL 8 schema adopted by Flyway at V5: " + database);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }

    private static String databaseName(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:mysql://")) {
            throw new IllegalArgumentException("DB_URL must use jdbc:mysql://");
        }
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String path = uri.getPath();
        if (path == null || path.length() < 2) throw new IllegalArgumentException("DB_URL must include database name");
        return path.substring(1);
    }
}
