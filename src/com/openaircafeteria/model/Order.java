package com.openaircafeteria.model;

import java.util.ArrayList;
import java.util.List;

/** A student's cafeteria order. */
public class Order {
    private final String studentName;
    private final String pickupSlot;
    private final List<OrderLine> lines;

    public Order(String studentName, String pickupSlot) {
        this.studentName = studentName;
        this.pickupSlot = pickupSlot;
        this.lines = new ArrayList<OrderLine>();
    }

    public void addItem(MenuItem item, int quantity) {
        lines.add(new OrderLine(item, quantity));
    }

    public String getStudentName() {
        return studentName;
    }

    public String getPickupSlot() {
        return pickupSlot;
    }

    public List<OrderLine> getLines() {
        return new ArrayList<OrderLine>(lines);
    }

    public int getTotal() {
        int total = 0;
        for (OrderLine line : lines) {
            total += line.getAmount();
        }
        return total;
    }

    public String getSummary() {
        return studentName + " | Pickup: " + pickupSlot + " | Total: Rs. " + getTotal();
    }
}
