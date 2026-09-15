package com.openaircafeteria.model;

/** A food item sold at Open Air Cafeteria. */
public class MenuItem {
    private final String name;
    private final int price;
    private final String category;
    private final String emoji;

    public MenuItem(String name, int price, String category, String emoji) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.emoji = emoji;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getEmoji() {
        return emoji;
    }

    @Override
    public String toString() {
        return name + " - Rs. " + price;
    }
}
