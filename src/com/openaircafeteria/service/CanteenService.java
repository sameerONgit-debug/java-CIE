package com.openaircafeteria.service;

import com.openaircafeteria.model.MenuItem;
import com.openaircafeteria.model.Order;
import com.openaircafeteria.model.OrderLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small service layer for the prototype. It keeps the menu and orders in
 * memory, which is enough for a college demonstration.
 */
public class CanteenService {
    private final List<MenuItem> menu;
    private final List<Order> orders;
    private int nextOrderNumber;

    public CanteenService() {
        this.menu = createMenu();
        this.orders = new ArrayList<Order>();
        this.nextOrderNumber = 1001;
    }

    private List<MenuItem> createMenu() {
        return new ArrayList<MenuItem>(Arrays.asList(
                new MenuItem("masala-dosa", "Masala Dosa",
                        "Crisp dosa with potato masala and chutney", 55,
                        "Meals", "🥞", true, true, 10, true),
                new MenuItem("paneer-roll", "Paneer Frankie",
                        "Spiced paneer, onions and mint chutney", 70,
                        "Quick bites", "🌯", true, true, 8, true),
                new MenuItem("veg-sandwich", "Grilled Sandwich",
                        "Toasted bread, vegetables and cheese", 60,
                        "Quick bites", "🥪", true, false, 7, true),
                new MenuItem("chole-bhature", "Chole Bhature",
                        "Comforting chole with two fluffy bhaturas", 85,
                        "Meals", "🍛", true, true, 12, true),
                new MenuItem("samosa", "Samosa",
                        "Golden potato and pea samosa", 25,
                        "Quick bites", "🔺", true, false, 5, true),
                new MenuItem("cold-coffee", "Cold Coffee",
                        "Chilled coffee topped with a creamy cloud", 45,
                        "Drinks", "🥤", true, true, 4, true),
                new MenuItem("nimbu-paani", "Nimbu Paani",
                        "Fresh lemon cooler with a pinch of mint", 30,
                        "Drinks", "🍋", true, false, 3, true),
                new MenuItem("gulab-jamun", "Gulab Jamun",
                        "Soft syrup-soaked dessert, served warm", 35,
                        "Desserts", "🍮", true, false, 3, true)
        ));
    }

    public synchronized Map<String, Object> getMenuResponse() {
        List<Map<String, Object>> menuMaps = new ArrayList<Map<String, Object>>();
        for (MenuItem item : menu) {
            menuMaps.add(item.toMap());
        }

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("canteenName", "Open Air Cafeteria");
        response.put("isOpen", true);
        response.put("menu", menuMaps);
        response.put("pickupSlots", Arrays.asList(
                "12:30 PM - 12:45 PM",
                "12:45 PM - 1:00 PM",
                "1:00 PM - 1:15 PM",
                "1:15 PM - 1:30 PM"
        ));
        response.put("ordersToday", orders.size());
        return response;
    }

    /**
     * Validates the simple order form and creates an Order object.
     */
    public synchronized Order placeOrder(String name, String email,
                                         String pickupSlot, String notes,
                                         Map<String, Integer> requestedItems) {
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Please enter your name");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Please enter a valid email");
        }
        if (pickupSlot == null || pickupSlot.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a pickup slot");
        }
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("Please add at least one item");
        }

        List<OrderLine> lines = new ArrayList<OrderLine>();
        for (Map.Entry<String, Integer> requested : requestedItems.entrySet()) {
            MenuItem item = findItem(requested.getKey());
            int quantity = requested.getValue() == null ? 0 : requested.getValue();
            if (item == null || !item.isAvailable()) {
                throw new IllegalArgumentException("One of the selected items is unavailable");
            }
            if (quantity < 1 || quantity > 10) {
                throw new IllegalArgumentException("Each item can be ordered up to 10 times");
            }
            lines.add(new OrderLine(item, quantity));
        }

        int number = nextOrderNumber++;
        String id = "order-" + number;
        String orderNumber = "OAC-" + number;
        String pickupCode = "" + (100 + (number % 900));
        Order order = new Order(id, orderNumber, pickupCode, name.trim(),
                email.trim(), pickupSlot.trim(), notes == null ? "" : notes.trim(), lines);
        orders.add(order);
        return order;
    }

    private MenuItem findItem(String id) {
        for (MenuItem item : menu) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public synchronized List<Map<String, Object>> getOrders() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Order order : orders) {
            result.add(order.toMap());
        }
        return result;
    }
}
