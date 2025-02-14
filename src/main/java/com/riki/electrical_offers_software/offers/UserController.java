package com.riki.electrical_offers_software.offers;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField lastnameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Button saveButton;

    @FXML
    private Button changePasswordButton;

    private int userId = 1; // Assuming a single user scenario

    @FXML
    public void initialize() {
        loadUserData();
        saveButton.setOnAction(event -> updateUserData());
        changePasswordButton.setOnAction(event -> openChangePasswordStage());
    }

    private void loadUserData() {
        String query = "SELECT id, first_name, last_name, email, phone_number FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfiguration.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("first_name"));
                lastnameField.setText(rs.getString("last_name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone_number"));
            } else {
                showAlert("Σφάλμα", "Ο χρήστης δεν βρέθηκε.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Σφάλμα", "Αποτυχία φόρτωσης δεδομένων χρήστη.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateUserData() {
        String query = "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone_number = ? WHERE id = ?";

        try (Connection conn = DatabaseConfiguration.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nameField.getText());
            stmt.setString(2, lastnameField.getText());
            stmt.setString(3, emailField.getText());
            stmt.setString(4, phoneField.getText());
            stmt.setInt(5, userId);
            stmt.executeUpdate();
            showAlert("Επιτυχία", "Τα δεδομένα του χρήστη ενημερώθηκαν με επιτυχία.", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Σφάλμα", "Αποτυχία ενημέρωσης δεδομένων χρήστη.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void openChangePasswordStage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/riki/electrical_offers_software/offers/ChangePassword.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Αλλαγή Κωδικού Πρόσβασης");
            stage.show();
        } catch (IOException e) {
            showAlert("Σφάλμα", "Αποτυχία φόρτωσης της φόρμας αλλαγής κωδικού.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}