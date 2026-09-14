package com.openaircafeteria;

import com.openaircafeteria.model.MenuItem;
import com.openaircafeteria.model.Order;

/**
 * Tiny console demo for the Java OOP part of the project.
 * The browser prototype uses the same idea: menu item -> order line -> order.
 */
public class CanteenApp {
    public static void main(String[] args) {
        MenuItem dosa = new MenuItem("Masala Dosa", 55, "Meals", "🥞");
        MenuItem coffee = new MenuItem("Cold Coffee", 45, "Drinks", "🥤");

        Order order = new Order("Aditi", "12:30 PM - 12:45 PM");
        order.addItem(dosa, 1);
        order.addItem(coffee, 1);

        System.out.println("OPEN AIR CAFETERIA");
        System.out.println("-------------------");
        System.out.println(dosa);
        System.out.println(coffee);
        System.out.println("\nOrder booked successfully!");
        System.out.println(order.getSummary());
    }
}
