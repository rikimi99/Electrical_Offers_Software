package com.riki.electrical_offers_software.offers;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChangePasswordController {

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button changePasswordButton;

    private int userId = 1;

    @FXML
    public void initialize() {
        changePasswordButton.setOnAction(event -> changePassword());
    }

    private void changePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Όλα τα πεδία είναι υποχρεωτικά.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Οι κωδικοί πρόσβασης δεν ταιριάζουν.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl())) {
            if (conn == null || conn.isClosed()) {
                showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Η σύνδεση με τη βάση δεδομένων απέτυχε.");
                return;
            }

            String query = "SELECT password FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (!BCrypt.checkpw(currentPassword, storedPassword)) {
                        showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Ο τρέχων κωδικός πρόσβασης είναι λανθασμένος.");
                        return;
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Ο χρήστης δεν βρέθηκε.");
                    return;
                }
            }

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            String updateQuery = "UPDATE users SET password = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setString(1, hashedPassword);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Επιτυχία", "Ο κωδικός πρόσβασης άλλαξε με επιτυχία.");
                ((Stage) changePasswordButton.getScene().getWindow()).close();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Σφάλμα", "Σφάλμα βάσης δεδομένων: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
