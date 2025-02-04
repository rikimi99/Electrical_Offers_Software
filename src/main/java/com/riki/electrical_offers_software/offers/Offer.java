package com.riki.electrical_offers_software.offers;

public class Offer {
    private final int id;
    private final String name;
    private int quantity;
    private final String categoryName;

    public Offer(int id, String name, int quantity, String categoryName) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.categoryName = categoryName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
