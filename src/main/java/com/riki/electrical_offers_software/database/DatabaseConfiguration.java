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
     * Establish a connection to the SQLite database.
     * If the connection doesn't exist, create it and initialize the database.
     * @return Active database connection
     */
    public static Connection connect() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DATABASE_URL);
                System.out.println("Connected to SQLite database successfully!");

                // Initialize the database (create tables if not exist)
                initializeDatabase();

            } catch (SQLException e) {
                System.out.println("Database connection failed: " + e.getMessage());
            }
        }
        return connection;
    }

    /**
     * Initializes the database by executing the `tables.sql` script.
     * Ensures tables exist before executing queries.
     */
    private static void initializeDatabase() {
        if (!initialized) {
            try (InputStream inputStream = DatabaseConfiguration.class.getResourceAsStream("/tables.sql");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                 Statement stmt = connection.createStatement()) {

                if (inputStream == null) {
                    throw new NullPointerException("SQL file not found: /tables.sql");
                }

                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sql.append(line).append("\n");
                }

                // Split and execute multiple SQL statements
                String[] sqlStatements = sql.toString().split(";");
                for (String statement : sqlStatements) {
                    String trimmedStatement = statement.trim();
                    if (!trimmedStatement.isEmpty()) {
                        stmt.execute(trimmedStatement);
                    }
                }

                initialized = true; // Mark database as initialized
                System.out.println("Database initialized successfully!");

            } catch (Exception e) {
                System.out.println("Error initializing database: " + e.getMessage());
            }
        }
    }

    /**
     * Close the database connection.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                initialized = false;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

}
