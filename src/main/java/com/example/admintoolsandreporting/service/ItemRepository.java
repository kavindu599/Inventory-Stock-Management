package com.example.admintoolsandreporting.service;

import com.example.admintoolsandreporting.model.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ItemRepository {

    private final Path itemsFilePath;

    public ItemRepository(@Value("${data.items.path}") String itemsPathString) {
        this.itemsFilePath = Paths.get(itemsPathString);
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            if (!Files.exists(itemsFilePath.getParent())) {
                Files.createDirectories(itemsFilePath.getParent());
            }
            if (!Files.exists(itemsFilePath)) {
                Files.createFile(itemsFilePath);
               
                List<String> sampleData = List.of(
                        new Item("Sample Laptop", "Electronics", 5, 1200.00, java.time.LocalDate.now().minusMonths(2), java.time.LocalDate.now().plusYears(2)).toFileString(),
                        new Item("Office Chair", "Furniture", 8, 150.00, java.time.LocalDate.now().minusMonths(1), null).toFileString(), // null expiry
                        new Item("Printer Paper", "Stationery", 3, 5.00, java.time.LocalDate.now().minusDays(10), null).toFileString(),
                        new Item("Expired Milk", "Groceries", 1, 2.00, java.time.LocalDate.now().minusDays(10), java.time.LocalDate.now().minusDays(2)).toFileString()
                );
                Files.write(itemsFilePath, sampleData, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            }
        } catch (IOException e) {
            System.err.println("Error ensuring items file exists: " + e.getMessage());
            
        }
    }

    public List<Item> findAll() {
        try {
            return Files.lines(itemsFilePath, StandardCharsets.UTF_8)
                    .map(Item::fromFileString)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading items file: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void saveAll(List<Item> items) {
        List<String> lines = items.stream()
                .map(Item::toFileString)
                .collect(Collectors.toList());
        try {
            Files.write(itemsFilePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.err.println("Error writing items file: " + e.getMessage());
        }
    }

}
