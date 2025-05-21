package com.example.admintoolsandreporting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalProducts; // Total distinct item types
    private long lowStockItemCount;
    private double totalInventoryValue;
}
