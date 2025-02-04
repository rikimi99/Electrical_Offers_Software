package com.riki.electrical_offers_software.offers;

import com.dlsc.pdfviewfx.PDFView;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;

public class PreviewPageController {

    @FXML
    private PDFView pdfViewer;

    @FXML
    private Button sendEmailButton;

    @FXML
    private Button closeButton;

    private byte[] pdfData;
    private String clientEmail;
    private String pdfFilePath;

    @FXML
    public void initialize() {
        sendEmailButton.setOnAction(event -> sendEmail());
        closeButton.setOnAction(event -> ((Stage) closeButton.getScene().getWindow()).close());
    }

    public void loadPDF(String filePath, String clientEmail) {
        try {
            this.clientEmail = clientEmail;
            this.pdfFilePath = new String(filePath.getBytes(), "UTF-8");  // Ensure UTF-8

            File pdfFile = new File(this.pdfFilePath);
            if (!pdfFile.exists()) {
                showAlert("Σφάλμα", "Το αρχείο PDF δεν βρέθηκε.", Alert.AlertType.ERROR);
                return;
            }

            pdfViewer.load(pdfFile);
            pdfData = Files.readAllBytes(pdfFile.toPath());
            showAlert("Επιτυχία", "Το PDF φορτώθηκε με επιτυχία.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Σφάλμα", "Αποτυχία φόρτωσης PDF.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void sendEmail() {
        if (pdfData == null || pdfData.length == 0 || clientEmail == null || clientEmail.isEmpty()) {
            showAlert("Σφάλμα", "Δεν υπάρχει έγκυρο email ή PDF για αποστολή.", Alert.AlertType.ERROR);
            return;
        }

        String subject = "Η προσφορά σας από το Electrical Offers Software";
        String messageContent = "<h2>Αγαπητέ πελάτη,</h2><p>Σας επισυνάπτουμε την προσφορά σας σε μορφή PDF.</p>";

        EmailSender.sendEmail(clientEmail, subject, messageContent, pdfFilePath);
        showAlert("Επιτυχία", "Το email στάλθηκε με επιτυχία στον πελάτη.", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
