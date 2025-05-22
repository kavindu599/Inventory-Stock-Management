package com.example.stockapp.service;

import com.example.stockapp.model.Item;
import com.example.stockapp.util.MergeSort;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class ItemService {

    private final String FILE_PATH = "src/main/resources/items.txt";

    public List<Item> getAllItems() {
        List<Item> items = readFromFile();
        markUrgent(items);
        return items;
    }

    public List<Item> getSortedItems() {
        List<Item> items = getAllItems();
        return MergeSort.sort(items);
    }

    public void addItem(Item item) {
        try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
            writer.write(item.getId() + "," + item.getName() + "," + item.getExpiryDate() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateItem(Item updatedItem) {
        List<Item> items = readFromFile();
        try (FileWriter writer = new FileWriter(FILE_PATH, false)) {
            for (Item item : items) {
                if (item.getId().equals(updatedItem.getId())) {
                    writer.write(updatedItem.getId() + "," + updatedItem.getName() + "," + updatedItem.getExpiryDate() + "\n");
                } else {
                    writer.write(item.getId() + "," + item.getName() + "," + item.getExpiryDate() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(String id) {
        List<Item> items = readFromFile();
        try (FileWriter writer = new FileWriter(FILE_PATH, false)) {
            for (Item item : items) {
                if (!item.getId().equals(id)) {
                    writer.write(item.getId() + "," + item.getName() + "," + item.getExpiryDate() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Item> readFromFile() {
        List<Item> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    Item item = new Item(parts[0], parts[1], LocalDate.parse(parts[2]), false);
                    items.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    private void markUrgent(List<Item> items) {
        LocalDate today = LocalDate.now();
        for (Item item : items) {
            item.setUrgent(item.getExpiryDate().isBefore(today.plusDays(4)));
        }
    }

    // ✅ New method to check if ID already exists
   public boolean isDuplicateId(String id) {
    List<Item> items = readFromFile();
    return items.stream().anyMatch(item -> item.getId().equalsIgnoreCase(id));
}

}
