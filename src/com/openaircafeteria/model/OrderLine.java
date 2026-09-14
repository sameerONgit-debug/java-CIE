package com.openaircafeteria.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One menu item and the number of portions requested in an order.
 */
public class OrderLine {
    private final MenuItem menuItem;
    private final int quantity;

    public OrderLine(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getLineTotal() {
        return menuItem.getPrice() * quantity;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> line = new LinkedHashMap<String, Object>();
        line.put("menuItemId", menuItem.getId());
        line.put("name", menuItem.getName());
        line.put("emoji", menuItem.getEmoji());
        line.put("quantity", quantity);
        line.put("unitPrice", menuItem.getPrice());
        line.put("lineTotal", getLineTotal());
        return line;
    }
}
