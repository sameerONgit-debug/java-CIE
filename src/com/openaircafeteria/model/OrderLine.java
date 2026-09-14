package com.openaircafeteria.model;

/** One menu item and its quantity inside an order. */
public class OrderLine {
    private final MenuItem item;
    private final int quantity;

    public OrderLine(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getAmount() {
        return item.getPrice() * quantity;
    }
}
