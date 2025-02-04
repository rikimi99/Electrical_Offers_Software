package com.riki.electrical_offers_software.offers;

import javafx.scene.control.Hyperlink;

import java.nio.charset.StandardCharsets;

public class OfferPDF {
    private final int id;
    private final String lastName;
    private final String createdAt;
    private final Hyperlink pdfLink;

    public OfferPDF(int id, String lastName, String createdAt, Hyperlink pdfLink) {
        this.id = id;
        this.lastName = new String(lastName.getBytes(), StandardCharsets.UTF_8);  // Encode properly
        this.createdAt = createdAt;
        this.pdfLink = pdfLink;
    }

    public int getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Hyperlink getPdfLink() {
        return pdfLink;
    }
}
