package com.example.admintoolsandreporting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportStats {
    private long lowStockItemCount;
    private long expiringSoonItemCount;
    private long expiredItemCount;
}
