package com.riki.electrical_offers_software.offers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PDFCreator {

    private static final String FONT_PATH = "src/main/resources/Arial.ttf";

    public static void generateOfferPDF(String filePath, List<Offer> offers, String name, String lastname, String email, String phone, String address, String companyName, String price) {
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            BaseFont baseFont = loadFont();
            Font titleFont = new Font(baseFont, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Font sectionFont = new Font(baseFont, 14, Font.BOLD, BaseColor.BLACK);
            Font headerFont = new Font(baseFont, 12, Font.BOLD, BaseColor.WHITE);
            Font cellFont = new Font(baseFont, 12, Font.NORMAL, BaseColor.BLACK);
            Font totalFont = new Font(baseFont, 14, Font.BOLD, BaseColor.RED);

            document.add(new Paragraph(companyName + "\nΠληροφορίες Προσφοράς", titleFont));
            document.add(new Paragraph(" "));
            document.add(new LineSeparator());

            addCustomerDetails(document, name, lastname, email, phone, address, cellFont);

            generateOfferTable(document, offers, sectionFont, headerFont, cellFont);

            document.add(new Paragraph("Συνολικό Ποσό: " + price , totalFont));
            document.add(new Paragraph(" "));

            addFooter(writer, baseFont);

            document.close();
            System.out.println("✅ PDF created successfully: " + filePath);

            storePDFInDatabase(filePath, lastname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BaseFont loadFont() throws DocumentException, IOException {
        try {
            return BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            System.err.println("❌ Error loading font. Using default font.");
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        }
    }

    private static void addCustomerDetails(Document document, String name, String lastname, String email, String phone, String address, Font font) throws DocumentException {
        document.add(new Paragraph("Στοιχεία Πελάτη", font));
        document.add(new Paragraph("Όνομα: " + name, font));
        document.add(new Paragraph("Επώνυμο: " + lastname, font));
        document.add(new Paragraph("Email: " + email, font));
        document.add(new Paragraph("Τηλέφωνο: " + phone, font));
        document.add(new Paragraph("Διεύθυνση: " + address, font));
        document.add(new Paragraph(" "));
    }

    private static void generateOfferTable(Document document, List<Offer> offers, Font sectionFont, Font headerFont, Font cellFont) throws DocumentException {
        Map<String, List<Offer>> groupedOffers = offers.stream().collect(Collectors.groupingBy(Offer::getCategoryName));

        for (Map.Entry<String, List<Offer>> entry : groupedOffers.entrySet()) {
            document.add(new Paragraph(entry.getKey(), sectionFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{40, 30, 30});

            addTableHeader(table, headerFont);

            int totalQuantity = 0;
            for (Offer offer : entry.getValue()) {
                addTableRow(table, offer, cellFont);
                totalQuantity += offer.getQuantity();
            }

            document.add(table);
            document.add(new Paragraph("Συνολική Ποσότητα: " + totalQuantity, sectionFont));
            document.add(new Paragraph(" "));
        }
    }

    private static void addTableHeader(PdfPTable table, Font headerFont) {
        String[] headers = {"Όνομα Υλικού", "Κατηγορία", "Ποσότητα"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private static void addTableRow(PdfPTable table, Offer offer, Font cellFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(offer.getName(), cellFont));
        PdfPCell cell2 = new PdfPCell(new Phrase(offer.getCategoryName(), cellFont));
        PdfPCell cell3 = new PdfPCell(new Phrase(String.valueOf(offer.getQuantity()), cellFont));

        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);
    }

    private static void addFooter(PdfWriter writer, BaseFont baseFont) {
        PdfContentByte canvas = writer.getDirectContent();
        Phrase footer = new Phrase("Εμπιστευτικό Επιχείρησης - Σελίδα ", new Font(baseFont, 10, Font.NORMAL, BaseColor.GRAY));
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, footer, 300, 20, 0);
    }

    private static void storePDFInDatabase(String filePath, String lastname) {
        try (Connection conn = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl());
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO offer_pdfs (last_name, pdf, file_path) VALUES (?, ?, ?)");
             FileInputStream fis = new FileInputStream(filePath)) {

            byte[] pdfData = Files.readAllBytes(Paths.get(filePath));
            pstmt.setString(1, lastname);
            pstmt.setBytes(2, pdfData);
            pstmt.setString(3, filePath);
            pstmt.executeUpdate();
            System.out.println("✅ PDF stored in database successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void downloadPDF(byte[] pdfData) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Αποθήκευση αρχείου PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfData);
                System.out.println("✅ PDF saved successfully at: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
