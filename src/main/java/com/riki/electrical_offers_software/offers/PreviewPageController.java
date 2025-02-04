package com.riki.electrical_offers_software.offers;

import com.dlsc.pdfviewfx.PDFView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
                System.out.println("❌ Error: PDF file not found.");
                return;
            }

            pdfViewer.load(pdfFile);
            pdfData = Files.readAllBytes(pdfFile.toPath());
            System.out.println("✅ PDF loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendEmail() {
        if (pdfData == null || pdfData.length == 0 || clientEmail == null || clientEmail.isEmpty()) {
            System.out.println("❌ No valid email or PDF to send.");
            return;
        }

        String subject = "Your Offer from Electrical Offers Software";
        String messageContent = "<h2>Dear Customer,</h2><p>Attached is your offer PDF.</p>";

        EmailSender.sendEmail(clientEmail, subject, messageContent, pdfFilePath);
        System.out.println("📧 Email sent to: " + clientEmail);
    }
}
