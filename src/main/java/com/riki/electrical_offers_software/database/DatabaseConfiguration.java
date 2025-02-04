package com.riki.electrical_offers_software.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfiguration {
    private static final String DATABASE_URL = "jdbc:sqlite:electrical_offers.db";
    private static Connection connection = null;
    private static boolean initialized = false;

    /**
     * Establish a persistent connection to the SQLite database.
     * @return Active database connection.
     */
    public static Connection connect() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DATABASE_URL);
                System.out.println("✅ Connected to SQLite database successfully!");

                initializeDatabase();

            } catch (SQLException e) {
                System.out.println("❌ Database connection failed: " + e.getMessage());
            }
        }
        return connection;
    }

    /**
     * Returns the database URL to be used in other classes.
     * @return The database URL as a String.
     */
    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }

    /**
     * Initializes the database by executing the `tables.sql` script.
     * Ensures tables exist before executing queries.
     */
    private static void initializeDatabase() {
        if (initialized || connection == null) return;

        try (InputStream inputStream = DatabaseConfiguration.class.getResourceAsStream("/tables.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             Statement stmt = connection.createStatement()) {

            if (inputStream == null) {
                System.out.println("❌ SQL file not found: /tables.sql");
                return;
            }

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }

            String[] sqlStatements = sql.toString().split(";");
            for (String statement : sqlStatements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    stmt.execute(trimmedStatement);
                }
            }

            initialized = true;
            System.out.println("✅ Database initialized successfully!");

        } catch (Exception e) {
            System.out.println("❌ Error initializing database: " + e.getMessage());
        }
    }
}