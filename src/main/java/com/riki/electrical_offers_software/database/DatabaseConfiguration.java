package com.riki.electrical_offers_software.database;

import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;

public class DatabaseConfiguration {
    private static final String DATABASE_URL = "jdbc:sqlite:electrical_offers.db";
    private static Connection connection = null;
    private static boolean initialized = false;

    private static final String ADMIN_FIRST_NAME = "Σπύρος";
    private static final String ADMIN_LAST_NAME = "Ζέλις";
    private static final String ADMIN_EMAIL = "spirosilekt@gmail.com";
    private static final String ADMIN_PASSWORD = "0000";
    private static final String ADMIN_PHONE = "+306940708557";

    public static Connection connect() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DATABASE_URL);
                System.out.println("✅ Connected to SQLite database successfully!");

                initializeDatabase();
                ensureAdminUserExists();

            } catch (SQLException e) {
                System.out.println("❌ Database connection failed: " + e.getMessage());
            }
        }
        return connection;
    }

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

    private static void ensureAdminUserExists() {
        if (connection == null) return;

        String checkUserQuery = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkUserQuery)) {
            pstmt.setString(1, ADMIN_EMAIL);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("🔹 Admin user already exists. No need to create.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Error checking admin user existence: " + e.getMessage());
            return;
        }

        String insertAdminSQL = "INSERT INTO users (first_name, last_name, email, password, phone_number) VALUES (?, ?, ?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(ADMIN_PASSWORD, BCrypt.gensalt(12));

        try (PreparedStatement pstmt = connection.prepareStatement(insertAdminSQL)) {
            pstmt.setString(1, ADMIN_FIRST_NAME);
            pstmt.setString(2, ADMIN_LAST_NAME);
            pstmt.setString(3, ADMIN_EMAIL);
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, ADMIN_PHONE);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Admin user created successfully!");
            } else {
                System.out.println("❌ Failed to create admin user.");
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Error while creating admin user: " + e.getMessage());
        }
    }

    /**
     * Returns the database URL to be used in other classes.
     * @return The database URL as a String.
     */
    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }
}
