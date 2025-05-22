package com.example.admintoolsandreporting.model;

import com.example.admintoolsandreporting.util.Constants;
import com.example.admintoolsandreporting.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private String id;
    private String name;
    private String category;
    private int quantity;
    private double price;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;

    // Constructor for creating new items without ID (ID generated)
    public Item(String name, String category, int quantity, double price, LocalDate purchaseDate, LocalDate expiryDate) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
    }

    // Encapsulated logic
    public String getStatus() {
        if (isExpired()) {
            return "Expired";
        }
        if (quantity <= 0) {
            return "Out of Stock";
        }
        if (quantity < Constants.LOW_STOCK_THRESHOLD) {
            return "Low Stock";
        }
        return "In Stock";
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon() {
        return expiryDate != null &&
                !isExpired() && 
                expiryDate.isBefore(LocalDate.now().plusDays(Constants.EXPIRING_SOON_DAYS_THRESHOLD + 1));
    }

    
    public String toFileString() {
        return String.join(",",
                id,
                name,
                category,
                String.valueOf(quantity),
                String.valueOf(price),
                DateUtils.formatDate(purchaseDate),
                DateUtils.formatDate(expiryDate)
        );
    }

    public static Item fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length < 7) return null;
        try {
            return new Item(
                    parts[0],
                    parts[1],
                    parts[2],
                    Integer.parseInt(parts[3]),
                    Double.parseDouble(parts[4]),
                    DateUtils.parseDate(parts[5]),
                    DateUtils.parseDate(parts[6])
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing item from line: " + line + " - " + e.getMessage());
            return null;
        }
    }
}
