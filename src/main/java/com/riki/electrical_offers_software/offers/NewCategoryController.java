package com.riki.electrical_offers_software.offers;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class NewCategoryController {

    @FXML
    private TextField categoryNameField;

    @FXML
    private Button addButton, cancelButton;

    @FXML
    public void initialize() {
        addButton.setOnAction(event -> addCategory());
        cancelButton.setOnAction(event -> closeWindow());
    }

    /**
     * Adds a new category to the database after validation.
     */
    private void addCategory() {
        String categoryName = categoryNameField.getText().trim();

        if (categoryName.isEmpty()) {
            showAlert("Μη έγκυρη εισαγωγή", "Παρακαλώ εισάγετε ένα όνομα κατηγορίας.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl())) {
            if (categoryExists(conn, categoryName)) {
                showAlert("Διπλότυπη κατηγορία", "Η κατηγορία υπάρχει ήδη! Δοκιμάστε ένα διαφορετικό όνομα.", Alert.AlertType.WARNING);
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO categories (name) VALUES (?)")) {
                stmt.setString(1, categoryName);
                stmt.executeUpdate();
                showAlert("Επιτυχία", "Η κατηγορία προστέθηκε με επιτυχία!", Alert.AlertType.INFORMATION);
                closeWindow();
            }
        } catch (SQLException e) {
            showAlert("Σφάλμα βάσης δεδομένων", "Αποτυχία προσθήκης κατηγορίας.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Checks if a category already exists in the database.
     */
    private boolean categoryExists(Connection conn, String name) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM categories WHERE LOWER(name) = LOWER(?)")) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Displays an alert dialog.
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Closes the window.
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}