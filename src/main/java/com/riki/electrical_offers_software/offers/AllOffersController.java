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

                // Create clickable hyperlink for downloading the PDF
                Hyperlink pdfLink = new Hyperlink("Download PDF");
                pdfLink.setOnAction(event -> downloadPDF(id));

                offersList.add(new OfferPDF(id, lastName, createdAt, pdfLink));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openSelectedPDF() {
        OfferPDF selectedOffer = offersHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            showAlert("Selection Error", "Please select an offer to open.", Alert.AlertType.WARNING);
            return;
        }

        String query = "SELECT pdf FROM offer_pdfs WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, selectedOffer.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Create a temporary file for opening
                    File tempFile = File.createTempFile("offer_", ".pdf");
                    try (InputStream is = rs.getBinaryStream("pdf");
                         FileOutputStream fos = new FileOutputStream(tempFile)) {
                        Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }

                    // Open the file based on the OS
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


    private void deleteSelectedOffer() {
        OfferPDF selectedOffer = offersHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedOffer == null) {
            showAlert("Selection Error", "Please select an offer to delete.", Alert.AlertType.WARNING);
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
