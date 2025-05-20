package com.example.admintoolsandreporting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemView {
    private String id;
    private String name;
    private String category;
    private int quantity;
    private String status; 
    private String expiryDate;

    public static ItemView fromItem(Item item) {
        return new ItemView(
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getQuantity(),
                item.getStatus(),
                item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A"
        );
    }
}
