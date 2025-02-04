package com.riki.electrical_offers_software.offers;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NewOfferController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField lastnameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private TableView<Material> materialsTable;

    @FXML
    private TableColumn<Material, String> nameColumn;

    @FXML
    private TableColumn<Material, String> categoryMaterialColumn;

    @FXML
    private TableView<Offer> offersTable;

    @FXML
    private TableColumn<Offer, String> materialColumn;

    @FXML
    private TableColumn<Offer, Integer> quantityColumn;

    @FXML
    private TableColumn<Offer, String> categoryColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button editButton;

    @FXML
    private Button createOfferButton;

    @FXML
    private TextField searchField;
    @FXML
    private TextField priceField;


    private static final ObservableList<Offer> offersList = FXCollections.observableArrayList();
    private final ObservableList<Material> materialsList = FXCollections.observableArrayList();
    private final ObservableList<Material> filteredMaterialsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryMaterialColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        materialColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        materialsTable.setItems(materialsList);
        materialsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        offersTable.setItems(offersList);
        offersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        addButton.setOnAction(event -> addSelectedMaterialToOffer());
        removeButton.setOnAction(event -> removeSelectedOffer());
        editButton.setOnAction(event -> editSelectedOfferQuantity());
        createOfferButton.setOnAction(event -> generateOfferPDF());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterMaterials(newValue));
        priceField.textProperty().addListener((observable, oldValue, newValue) -> formatPriceInput(newValue, oldValue));
        loadMaterials();
    }

    private void loadMaterials() {
        materialsList.clear();
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT materials.id, materials.name, categories.name AS category_name FROM materials " +
                             "JOIN categories ON materials.category_id = categories.id")) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = new String(rs.getBytes("name"), "UTF-8");  // Ensure UTF-8
                String categoryName = new String(rs.getBytes("category_name"), "UTF-8");
                materialsList.add(new Material(id, name, categoryName));
            }
            materialsTable.setItems(materialsList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterMaterials(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            materialsTable.setItems(materialsList);
            return;
        }
        filteredMaterialsList.clear();
        String lowerCaseFilter = searchText.toLowerCase();

        for (Material material : materialsList) {
            if (material.getName().toLowerCase().contains(lowerCaseFilter)) {
                filteredMaterialsList.add(material);
            }
        }
        materialsTable.setItems(filteredMaterialsList);
    }

    private void addSelectedMaterialToOffer() {
        Material selectedMaterial = materialsTable.getSelectionModel().getSelectedItem();
        if (selectedMaterial == null) {
            showAlert("Σφάλμα Επιλογής", "Παρακαλώ επιλέξτε ένα υλικό για προσθήκη.", Alert.AlertType.WARNING);            return;
        }

        for (Offer offer : offersList) {
            if (offer.getId() == selectedMaterial.getId()) {
                offer.setQuantity(offer.getQuantity() + 1);
                offersTable.refresh();
                return;
            }
        }

        Offer newOffer = new Offer(selectedMaterial.getId(), selectedMaterial.getName(), 1, selectedMaterial.getCategoryName());
        offersList.add(newOffer);
    }

    private void removeSelectedOffer() {
        Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            showAlert("Σφάλμα Επιλογής", "Παρακαλώ επιλέξτε μια προσφορά για αφαίρεση.", Alert.AlertType.WARNING);            return;
        }
        offersList.remove(selectedOffer);
    }

    private void editSelectedOfferQuantity() {
        Offer selectedOffer = offersTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            showAlert("Σφάλμα Επιλογής", "Παρακαλώ επιλέξτε μια προσφορά για επεξεργασία.", Alert.AlertType.WARNING);            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedOffer.getQuantity()));
        dialog.setTitle("Επεξεργασία Ποσότητας");
        dialog.setHeaderText("Επεξεργαστείτε την ποσότητα της επιλεγμένης προσφοράς");
        dialog.setContentText("Εισαγάγετε νέα ποσότητα:");


        dialog.showAndWait().ifPresent(input -> {
            try {
                int newQuantity = Integer.parseInt(input);
                if (newQuantity < 0) {
                    showAlert("Μη Έγκυρη Εισαγωγή", "Η ποσότητα δεν μπορεί να είναι αρνητική.", Alert.AlertType.WARNING);
                } else {
                    selectedOffer.setQuantity(newQuantity);
                    offersTable.refresh();
                }
            } catch (NumberFormatException e) {
                showAlert("Μη Έγκυρη Εισαγωγή", "Παρακαλώ εισαγάγετε έναν έγκυρο αριθμό.", Alert.AlertType.WARNING);
            }
        });
    }

    private void generateOfferPDF() {
        if (offersList.isEmpty()) {
            showAlert("Δεν Υπάρχουν Προσφορές", "Δεν υπάρχουν προσφορές για δημιουργία PDF.", Alert.AlertType.WARNING);
            return;
        }

        if (isCustomerInfoMissing()) {
            showAlert("Ελλιπείς Πληροφορίες", "Παρακαλώ συμπληρώστε όλα τα στοιχεία του πελάτη πριν τη δημιουργία του PDF.", Alert.AlertType.WARNING);
            return;
        }

        String lastName = lastnameField.getText().trim();
        String directoryPath = "offers";
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filePath = directoryPath + "/" + lastName + "_" + System.currentTimeMillis() + ".pdf";
        String companyName = "";

        // Convert ObservableList to a regular List
        List<Offer> offerList = new ArrayList<>(offersList);

        // Extract and format price correctly
        String priceText = priceField.getText().trim();
        String formattedPrice = formatPriceForDisplay(priceText); // Ensure correct format

        // ✅ Generates PDF & Stores it in DB inside PDFCreator
        PDFCreator.generateOfferPDF(
                filePath,
                offerList, // Converted to List<Offer>
                nameField.getText(),
                lastName,
                emailField.getText(),
                phoneField.getText(),
                addressField.getText(),
                companyName,
                formattedPrice + " + ΦΠΑ" // Display price properly with VAT mention
        );

        showAlert("PDF Δημιουργήθηκε", "Το PDF προσφοράς δημιουργήθηκε με επιτυχία!", Alert.AlertType.INFORMATION);
        openPreviewPage(filePath);
    }

    // **Helper method to ensure correct price formatting**
    private String formatPriceForDisplay(String price) {
        if (price == null || price.isEmpty()) {
            return "0,00"; // Default value if empty
        }

        price = price.replace(".", "").replace(",", "."); // Convert European format to standard double
        try {
            double parsedPrice = Double.parseDouble(price);
            return String.format("%,.2f", parsedPrice).replace(",", "X").replace(".", ",").replace("X", ".");
        } catch (NumberFormatException e) {
            return "0,00"; // Default to 0 if parsing fails
        }
    }

    private void openPreviewPage(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            showAlert("Σφάλμα", "Δεν βρέθηκε PDF για προεπισκόπηση.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/riki/electrical_offers_software/offers/PreviewPage.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("PDF Preview");

            PreviewPageController controller = loader.getController();
            String clientEmail = emailField.getText().trim();  // Extract client email
            controller.loadPDF(filePath, clientEmail);  // Pass email to preview page

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isCustomerInfoMissing() {
        return nameField.getText().trim().isEmpty() ||
                lastnameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty() ||
                addressField.getText().trim().isEmpty();
    }

    private void formatPriceInput(String newValue, String oldValue) {
        if (newValue.isEmpty()) {
            return;
        }

        // Ensure valid characters: numbers (0-9), dot (.) for thousands, and comma (,) for decimals
        if (!newValue.matches("[0-9.,]*")) {
            priceField.setText(oldValue);
            return;
        }

        // Preserve caret position before formatting
        int caretPosition = priceField.getCaretPosition();

        // Prevent multiple commas (only one allowed for decimals)
        long commaCount = newValue.chars().filter(ch -> ch == ',').count();
        if (commaCount > 1) {
            priceField.setText(oldValue);
            return;
        }

        // Allow users to type a comma without interfering immediately
        if (newValue.endsWith(",")) {
            priceField.setText(newValue);
            priceField.positionCaret(caretPosition);
            return;
        }

        // Split number into integer and decimal parts
        String[] parts = newValue.split(",");
        String integerPart = parts[0].replace(".", ""); // Remove existing dots before reformatting

        // Format integer part with thousand separators (e.g., "11000" → "11.000")
        String formattedIntPart = formatWithThousandSeparator(integerPart);

        // Rebuild formatted price
        String formattedPrice = formattedIntPart;
        if (parts.length > 1) {
            // Ensure only two decimal places are allowed
            String decimalPart = parts[1].replaceAll("[^0-9]", ""); // Remove invalid characters from decimal part
            if (decimalPart.length() > 2) {
                decimalPart = decimalPart.substring(0, 2); // Limit to two decimals
            }
            formattedPrice += "," + decimalPart;
        }

        // Update text safely
        priceField.setText(formattedPrice);

        // Restore caret position after formatting
        if (caretPosition < formattedPrice.length()) {
            priceField.positionCaret(caretPosition);
        } else {
            priceField.positionCaret(formattedPrice.length());
        }
    }

    private String formatWithThousandSeparator(String number) {
        if (number.isEmpty()) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();
        int count = 0;

        for (int i = number.length() - 1; i >= 0; i--) {
            formatted.insert(0, number.charAt(i));
            count++;
            if (count % 3 == 0 && i > 0) {
                formatted.insert(0, ".");
            }
        }

        return formatted.toString();
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}