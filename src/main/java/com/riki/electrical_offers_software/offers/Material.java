package com.riki.electrical_offers_software.offers;

public class Material {
    private final int id;
    private final String name;
    private final String categoryName;

    public Material(int id, String name, String categoryName) {
        this.id = id;
        this.name = name;
        this.categoryName = categoryName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
