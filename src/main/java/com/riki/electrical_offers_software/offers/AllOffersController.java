package com.riki.electrical_offers_software.offers;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

/**
 * The AllOffersController class manages the user interface for viewing, opening,
 * downloading, and deleting offer PDFs stored in the database.
 */
public class AllOffersController {

    @FXML
    private TableView<OfferPDF> offersHistoryTable;
    @FXML
    private TableColumn<OfferPDF, Integer> columnOfferId;
    @FXML
    private TableColumn<OfferPDF, String> columnLastName;
    @FXML
    private TableColumn<OfferPDF, Hyperlink> columnPdfName;
    @FXML
    private TableColumn<OfferPDF, String> columnCreatedAt;
    @FXML
    private Button openButton;
    @FXML
    private Button deleteButton;

    private final ObservableList<OfferPDF> offersList = FXCollections.observableArrayList();

    /**
     * Initializes the table view by setting up column mappings and loading offer data.
     */
    @FXML
    public void initialize() {
        columnOfferId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        columnCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        columnPdfName.setCellValueFactory(new PropertyValueFactory<>("pdfLink"));

        offersHistoryTable.setItems(offersList);
        offersHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        loadOffers();
        openButton.setOnAction(event -> openSelectedPDF());
        deleteButton.setOnAction(event -> deleteSelectedOffer());
    }

    /**
     * Loads all offers from the database and populates the table.
     */
    private void loadOffers() {
        offersList.clear();
        String query = "SELECT id, last_name, created_at FROM offer_pdfs";
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String lastName = rs.getString("last_name");
                String createdAt = rs.getString("created_at");

                Hyperlink pdfLink = new Hyperlink("Λήψη PDF");
                pdfLink.setOnAction(event -> downloadPDF(id));

                offersList.add(new OfferPDF(id, lastName, createdAt, pdfLink));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the selected offer PDF from the database.
     */
    private void openSelectedPDF() {
        OfferPDF selectedOffer = offersHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            showAlert("Σφάλμα επιλογής", "Παρακαλώ επιλέξτε μια προσφορά για άνοιγμα.", Alert.AlertType.WARNING);
            return;
        }

        String query = "SELECT pdf FROM offer_pdfs WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, selectedOffer.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    File tempFile = File.createTempFile("offer_", ".pdf");
                    try (InputStream is = rs.getBinaryStream("pdf");
                         FileOutputStream fos = new FileOutputStream(tempFile)) {
                        Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }

                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                        new ProcessBuilder("cmd", "/c", tempFile.getAbsolutePath()).start();
                    } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                        new ProcessBuilder("open", tempFile.getAbsolutePath()).start();
                    } else {
                        new ProcessBuilder("xdg-open", tempFile.getAbsolutePath()).start();
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloads the selected offer PDF from the database.
     */
    private void downloadPDF(int offerId) {
        String query = "SELECT pdf FROM offer_pdfs WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, offerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] pdfData = rs.getBytes("pdf");
                    if (pdfData != null) {
                        PDFCreator.downloadPDF(pdfData);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the selected offer from the database.
     */
    private void deleteSelectedOffer() {
        OfferPDF selectedOffer = offersHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            showAlert("Σφάλμα επιλογής", "Παρακαλώ επιλέξτε μια προσφορά για διαγραφή.", Alert.AlertType.WARNING);
            return;
        }
        String query = "DELETE FROM offer_pdfs WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, selectedOffer.getId());
            stmt.executeUpdate();
            offersList.remove(selectedOffer);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an alert message.
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
