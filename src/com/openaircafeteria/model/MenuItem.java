package com.openaircafeteria.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single item that can be ordered from the cafeteria menu.
 */
public class MenuItem {
    private final String id;
    private final String name;
    private final String description;
    private final int price;
    private final String category;
    private final String emoji;
    private final boolean vegetarian;
    private final boolean popular;
    private final int prepMinutes;
    private final boolean available;

    public MenuItem(String id, String name, String description, int price,
                    String category, String emoji, boolean vegetarian,
                    boolean popular, int prepMinutes, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.emoji = emoji;
        this.vegetarian = vegetarian;
        this.popular = popular;
        this.prepMinutes = prepMinutes;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public boolean isVegetarian() {
        return vegetarian;
    }

    public boolean isPopular() {
        return popular;
    }

    public int getPrepMinutes() {
        return prepMinutes;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Converts the object to data that can be serialized by the tiny JSON helper.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("id", id);
        item.put("name", name);
        item.put("description", description);
        item.put("price", price);
        item.put("category", category);
        item.put("emoji", emoji);
        item.put("vegetarian", vegetarian);
        item.put("popular", popular);
        item.put("prepMinutes", prepMinutes);
        item.put("available", available);
        return item;
    }
}
