package com.riki.electrical_offers_software.offers;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class NewMaterialController {

    @FXML
    private TextField materialName;

    @FXML
    private ChoiceBox<String> categoryChoiceBox;

    @FXML
    private Button addButton, cancelButton;

    private final ObservableList<String> categoriesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCategories();
        addButton.setOnAction(event -> addMaterial());
        cancelButton.setOnAction(event -> closeWindow());
    }

    /**
     * Loads categories from the database into the ChoiceBox.
     */
    void loadCategories() {
        categoriesList.clear(); // Clear to prevent duplicates

        // Get database URL from DatabaseConfiguration
        String dbPath = DatabaseConfiguration.getDatabaseUrl();
        System.out.println("Using database: " + dbPath);

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM categories");
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Loading categories...");
            boolean found = false;

            while (rs.next()) {
                String categoryName = rs.getString("name");
                System.out.println("Category found: " + categoryName);
                categoriesList.add(categoryName);
                found = true;
            }

            if (!found) {
                System.out.println("No categories found in the database!");
                showAlert("No Categories", "No categories found! Add categories first.", Alert.AlertType.WARNING);
            } else {
                categoryChoiceBox.setItems(categoriesList);
                categoryChoiceBox.getSelectionModel().selectFirst(); // Default to first category
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load categories.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Adds a new material to the database after validation.
     */
    private void addMaterial() {
        String name = materialName.getText().trim();
        String category = categoryChoiceBox.getValue();

        if (name.isEmpty() || category == null) {
            showAlert("Invalid Input", "Please enter a material name and select a category.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl())) {
            if (materialExists(conn, name)) {
                showAlert("Duplicate Material", "Material already exists! Try a different name.", Alert.AlertType.WARNING);
                return;
            }

            int categoryId = getCategoryId(conn, category);
            if (categoryId == -1) {
                showAlert("Error", "Selected category is invalid.", Alert.AlertType.ERROR);
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO materials (name, category_id) VALUES (?, ?)")) {
                stmt.setString(1, name);
                stmt.setInt(2, categoryId);
                stmt.executeUpdate();
                showAlert("Success", "Material added successfully!", Alert.AlertType.INFORMATION);
                closeWindow();
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add material.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Checks if a material already exists in the database.
     */
    private boolean materialExists(Connection conn, String name) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM materials WHERE LOWER(name) = LOWER(?)")) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Retrieves the category ID for a given category name.
     */
    private int getCategoryId(Connection conn, String categoryName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM categories WHERE name = ?")) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
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
