package com.riki.electrical_offers_software.users;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserCreation {

    private static final String ADMIN_FIRST_NAME = "Σπύρος";
    private static final String ADMIN_LAST_NAME = "Ζέλις";
    private static final String ADMIN_EMAIL = "spirosilekt@gmail.com";
    private static final String ADMIN_PASSWORD = "0000";
    private static final String ADMIN_PHONE = "+306940708557";


    /**
     * Creates an admin user if they do not already exist in the database.
     */
    public static void createAdminUser() {
        Connection conn = DatabaseConfiguration.connect();

        if (conn == null) {
            System.out.println("Database connection failed. Cannot create admin user.");
            return;
        }

        try {
            if (userExists(conn, ADMIN_EMAIL)) {
                System.out.println("Admin user already exists. Skipping creation.");
            } else {
                String hashedPassword = hashPassword(ADMIN_PASSWORD);
                String insertSQL = "INSERT INTO users (first_name, last_name, email, password, phone_number) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    pstmt.setString(1, ADMIN_FIRST_NAME);
                    pstmt.setString(2, ADMIN_LAST_NAME);
                    pstmt.setString(3, ADMIN_EMAIL);
                    pstmt.setString(4, hashedPassword);
                    pstmt.setString(5, ADMIN_PHONE);

                    int rowsInserted = pstmt.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("Admin user created successfully!");
                    } else {
                        System.out.println("Failed to create admin user.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while creating admin user: " + e.getMessage());
        } finally {
            DatabaseConfiguration.closeConnection();
        }
    }

    /**
     * Checks if a user exists in the database by email.
     * @param conn Active database connection.
     * @param email Email to check.
     * @return True if the user exists, false otherwise.
     */
    private static boolean userExists(Connection conn, String email) {
        String query = "SELECT id FROM users WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hashes a password using BCrypt.
     * @param password Plain text password.
     * @return Hashed password.
     */
    private static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
}
