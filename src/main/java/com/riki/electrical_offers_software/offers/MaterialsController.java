package com.riki.electrical_offers_software.offers;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class MaterialsController {

    @FXML
    private TableView<Category> categoriesTable;
    @FXML
    private TableColumn<Category, String> categoryNameColumn;

    @FXML
    private TableView<Material> materialsTable;
    @FXML
    private TableColumn<Material, String> materialNameColumn;
    @FXML
    private TableColumn<Material, String> materialCategoryColumn;

    @FXML
    private Button addMaterialButton;
    @FXML
    private Button removeMaterialButton;
    @FXML
    private Button addCategoryButton;
    @FXML
    private Button removeCategoryButton;

    private final ObservableList<Category> categoriesList = FXCollections.observableArrayList();
    private final ObservableList<Material> materialsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        materialNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        materialCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        categoriesTable.setItems(categoriesList);
        materialsTable.setItems(materialsList);

        loadCategories();
        loadMaterials();

        addMaterialButton.setOnAction(event -> openNewMaterialWindow());
        removeMaterialButton.setOnAction(event -> removeSelectedMaterial());
        addCategoryButton.setOnAction(event -> openNewCategoryWindow());
        removeCategoryButton.setOnAction(event -> removeSelectedCategory());
    }

    private void loadCategories() {
        categoriesList.clear();
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM categories");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                categoriesList.add(new Category(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMaterials() {
        materialsList.clear();
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT materials.id, materials.name, categories.name AS category_name " +
                             "FROM materials JOIN categories ON materials.category_id = categories.id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String categoryName = rs.getString("category_name");
                materialsList.add(new Material(id, name, categoryName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void openNewMaterialWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/riki/electrical_offers_software/offers/newMaterial.fxml"));
            Parent root = loader.load();
            NewMaterialController controller = loader.getController();
            controller.loadCategories();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Material");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadMaterials();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openNewCategoryWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/riki/electrical_offers_software/offers/newCategory.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Category");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadCategories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeSelectedCategory() {
        Category selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) return;
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM categories WHERE id = ?")) {
            stmt.setInt(1, selectedCategory.getId());
            stmt.executeUpdate();
            categoriesList.remove(selectedCategory);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeSelectedMaterial() {
        Material selectedMaterial = materialsTable.getSelectionModel().getSelectedItem();
        if (selectedMaterial == null) return;
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM materials WHERE id = ?")) {
            stmt.setInt(1, selectedMaterial.getId());
            stmt.executeUpdate();
            materialsList.remove(selectedMaterial);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
