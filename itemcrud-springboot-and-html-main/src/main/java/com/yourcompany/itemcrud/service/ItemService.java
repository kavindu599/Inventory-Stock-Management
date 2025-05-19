package com.yourcompany.itemcrud.service;

import com.yourcompany.itemcrud.model.Item;
import com.yourcompany.itemcrud.model.NonPerishableItem;
import com.yourcompany.itemcrud.model.PerishableItem;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {

    private static final String FILE = "items.txt";

    public List<Item> loadItems() throws IOException {
        List<Item> items = new ArrayList<>();

        // Create the file if it doesn't exist
        File file = new File(FILE);
        if (!file.exists()) {
            file.createNewFile();
            return items;
        }

        List<String> lines = Files.readAllLines(Paths.get(FILE));

        for (String line : lines) {
            if (line.trim().isEmpty()) continue; // Skip empty lines

            String[] parts = line.split(",");
            if (parts.length < 4) continue; // Skip malformed lines

            try {
                if (parts[0].equals("P") && parts.length == 5) {
                    items.add(new PerishableItem(parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]));
                } else if (parts[0].equals("N") && parts.length == 4) {
                    items.add(new NonPerishableItem(parts[1], parts[2], Integer.parseInt(parts[3])));
                }
            } catch (NumberFormatException e) {
                // Log or handle parsing errors
                System.err.println("Error parsing line: " + line);
            }
        }
        return items;
    }

    public void saveItems(List<Item> items) throws IOException {
        // Use try-with-resources to ensure the writer is closed
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Item item : items) {
                if (item instanceof PerishableItem) {
                    PerishableItem p = (PerishableItem) item;
                    writer.write(String.format("P,%s,%s,%d,%s\n",
                            p.getId(), p.getName(), p.getQuantity(), p.getExpiryDate()));
                } else {
                    writer.write(String.format("N,%s,%s,%d\n",
                            item.getId(), item.getName(), item.getQuantity()));
                }
            }
        }
    }

    public void addItem(Item item) throws IOException {
        List<Item> items = loadItems();

        // Check if an item with the same ID already exists
        items.removeIf(existingItem -> existingItem.getId().equals(item.getId()));

        items.add(item);
        saveItems(items);
    }

    public void deleteItem(String id) throws IOException {
        List<Item> items = loadItems();
        items.removeIf(item -> item.getId().equals(id));
        saveItems(items);
    }

    public void updateItem(String id, int quantity, String expiryDate) throws IOException {
        List<Item> items = loadItems();
        for (Item item : items) {
            if (item.getId().equals(id)) {
                item.setQuantity(quantity);
                if (item instanceof PerishableItem && expiryDate != null && !expiryDate.isEmpty()) {
                    ((PerishableItem) item).setExpiryDate(expiryDate);
                }
                break;
            }
        }
        saveItems(items);
    }
}