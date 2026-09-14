package com.openaircafeteria.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An order placed by a student. The order owns its lines and calculates totals.
 */
public class Order {
    private final String id;
    private final String orderNumber;
    private final String pickupCode;
    private final String studentName;
    private final String studentEmail;
    private final String pickupSlot;
    private final String notes;
    private final List<OrderLine> lines;
    private final Instant createdAt;
    private String status;

    public Order(String id, String orderNumber, String pickupCode,
                 String studentName, String studentEmail, String pickupSlot,
                 String notes, List<OrderLine> lines) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.pickupCode = pickupCode;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.pickupSlot = pickupSlot;
        this.notes = notes;
        this.lines = new ArrayList<OrderLine>(lines);
        this.createdAt = Instant.now();
        this.status = "confirmed";
    }

    public String getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getPickupSlot() {
        return pickupSlot;
    }

    public String getNotes() {
        return notes;
    }

    public List<OrderLine> getLines() {
        return new ArrayList<OrderLine>(lines);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public synchronized String getStatus() {
        return status;
    }

    public synchronized void setStatus(String status) {
        this.status = status;
    }

    public int getSubtotal() {
        int subtotal = 0;
        for (OrderLine line : lines) {
            subtotal += line.getLineTotal();
        }
        return subtotal;
    }

    public int getTotal() {
        // There is no convenience fee: students pay exactly the menu price.
        return getSubtotal();
    }

    public int getItemCount() {
        int count = 0;
        for (OrderLine line : lines) {
            count += line.getQuantity();
        }
        return count;
    }

    public int getEstimatedReadyIn() {
        if ("ready".equals(status) || "completed".equals(status)
                || "cancelled".equals(status)) {
            return 0;
        }
        if ("preparing".equals(status)) {
            return 8;
        }
        return 15;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> itemMaps = new ArrayList<Map<String, Object>>();
        for (OrderLine line : lines) {
            itemMaps.add(line.toMap());
        }

        result.put("id", id);
        result.put("orderNumber", orderNumber);
        result.put("pickupCode", pickupCode);
        result.put("studentName", studentName);
        result.put("studentEmail", studentEmail);
        result.put("pickupSlot", pickupSlot);
        result.put("notes", notes);
        result.put("items", itemMaps);
        result.put("itemCount", getItemCount());
        result.put("subtotal", getSubtotal());
        result.put("total", getTotal());
        result.put("createdAt", createdAt.toString());
        result.put("status", getStatus());
        result.put("estimatedReadyIn", getEstimatedReadyIn());
        return result;
    }
}
