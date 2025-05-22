package com.example.inventory_stack_management.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal; // For price

@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Generates no-args constructor
@AllArgsConstructor // Lombok: Generates all-args constructor
public class ItemDto {
    private String itemCode; // Unique identifier
    private String name;
    private Integer quantity;
    private BigDecimal unitPrice; // Use BigDecimal for currency
    private String category;
    private String description;

    // Optional: a transient method to calculate total value if needed often
    // Or calculate it in the service/template
    public BigDecimal getTotalValue() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}
