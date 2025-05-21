package com.example.admintoolsandreporting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalProducts;
    private long lowStockItemCount;
    private double totalInventoryValue;
}
