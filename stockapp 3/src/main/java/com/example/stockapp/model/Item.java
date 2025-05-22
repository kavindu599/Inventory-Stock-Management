package com.example.stockapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private String id;
    private String name;
    private LocalDate expiryDate;
    private boolean urgent;
}
