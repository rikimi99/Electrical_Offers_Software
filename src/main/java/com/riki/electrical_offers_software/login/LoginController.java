package com.riki.electrical_offers_software.login;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    private static final String ADMIN_EMAIL = "spirosilekt@gmail.com";

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button closeButton;

    /**
     * Event handler for login button
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String enteredPassword = passwordField.getText();

        if (enteredPassword.isEmpty()) {
            showAlert("Σφάλμα", "Παρακαλώ εισάγετε κωδικό.", Alert.AlertType.ERROR);
            return;
        }

        // Check user credentials
        if (authenticateUser(ADMIN_EMAIL, enteredPassword)) {
            showAlert("Επιτυχία", "Σύνδεση επιτυχής! Καλώς ήρθατε!", Alert.AlertType.INFORMATION);
            loadMainScreen();
        } else {
            showAlert("Αποτυχία", "Λάθος κωδικός. Δοκιμάστε ξανά.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Event handler for close button
     */
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Authenticate the user by checking the hashed password in the database.
     * @param email Admin email
     * @param enteredPassword Password entered by the user
     * @return true if authentication is successful, false otherwise
     */
    private boolean authenticateUser(String email, String enteredPassword) {
        Connection conn = DatabaseConfiguration.connect();

        if (conn == null) {
            System.out.println("Database connection failed.");
            return false;
        }

        String query = "SELECT password FROM users WHERE email = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password");
                    return BCrypt.checkpw(enteredPassword, storedHashedPassword);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during authentication: " + e.getMessage());
        }
        return false;
    }

    private void loadMainScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/riki/electrical_offers_software/offers/mainPage.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Electrical Offers Software");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show(); // Show the new window
            Stage loginStage = (Stage) passwordField.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace(); // Print errors for debugging
        }
    }


    /**
     * Show an alert dialog.
     * @param title Alert title
     * @param message Alert message
     * @param alertType Type of alert
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
