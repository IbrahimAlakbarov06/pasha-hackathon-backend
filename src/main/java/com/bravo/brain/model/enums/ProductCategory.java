package com.bravo.brain.model.enums;

import lombok.Getter;

@Getter
public enum ProductCategory {
    FRUIT("Meyvə", 7),
    VEGETABLE("Tərəvəz", 5),
    MEAT("Ət və Toyuq", 2),
    DAIRY("Süd məhsulları", 4),
    BREAD("Çörək və Pastry", 1),
    EGG("Yumurta", 10),
    CONFECTIONERY("Şirniyyat", 60),
    BEVERAGE("İçki", 90),
    OTHER("Digər", 30);

    private final String label;
    private final int defaultShelfDays; // default saxlama günü

    ProductCategory(String label, int defaultShelfDays) {
        this.label = label;
        this.defaultShelfDays = defaultShelfDays;
    }
}