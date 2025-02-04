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
     * Διαχειριστής συμβάντος για το κουμπί σύνδεσης.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String enteredPassword = passwordField.getText();

        if (enteredPassword.isEmpty()) {
            showAlert("Σφάλμα", "Παρακαλώ εισάγετε κωδικό.", Alert.AlertType.ERROR);
            return;
        }

        // Έλεγχος διαπιστευτηρίων χρήστη
        if (authenticateUser(ADMIN_EMAIL, enteredPassword)) {
            showAlert("Επιτυχία", "Σύνδεση επιτυχής! Καλώς ήρθατε!", Alert.AlertType.INFORMATION);
            loadMainScreen();
        } else {
            showAlert("Αποτυχία", "Λάθος κωδικός. Δοκιμάστε ξανά.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Διαχειριστής συμβάντος για το κουμπί κλεισίματος.
     */
    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Πιστοποίηση του χρήστη ελέγχοντας τον αποθηκευμένο κρυπτογραφημένο κωδικό στη βάση δεδομένων.
     * @param email Email διαχειριστή
     * @param enteredPassword Κωδικός που εισήγαγε ο χρήστης
     * @return true αν η πιστοποίηση είναι επιτυχής, false διαφορετικά
     */
    private boolean authenticateUser(String email, String enteredPassword) {
        Connection conn = DatabaseConfiguration.connect();

        if (conn == null) {
            System.out.println("Η σύνδεση με τη βάση δεδομένων απέτυχε.");
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
            System.out.println("Σφάλμα κατά την πιστοποίηση: " + e.getMessage());
        }
        return false;
    }

    /**
     * Φόρτωση της κύριας οθόνης μετά από επιτυχή σύνδεση.
     */
    private void loadMainScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/riki/electrical_offers_software/offers/mainPage.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Λογισμικό Προσφορών Ηλεκτρολογικών Ειδών");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show(); // Εμφάνιση του νέου παραθύρου
            Stage loginStage = (Stage) passwordField.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace(); // Εκτύπωση σφαλμάτων για αποσφαλμάτωση
        }
    }

    /**
     * Εμφάνιση προειδοποιητικού παραθύρου (alert).
     * @param title Τίτλος του alert
     * @param message Μήνυμα του alert
     * @param alertType Τύπος alert
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
