package com.yourcompany.itemcrud.controller;

import com.yourcompany.itemcrud.model.Item;
import com.yourcompany.itemcrud.model.NonPerishableItem;
import com.yourcompany.itemcrud.model.PerishableItem;
import com.yourcompany.itemcrud.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*") // Add this to allow cross-origin requests
public class ItemController {

    @Autowired
    private ItemService itemService;

    // Get all items
    @GetMapping
    public ResponseEntity<List<Item>> listItems() {
        try {
            return ResponseEntity.ok(itemService.loadItems());
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Get a specific item by ID
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable String id) {
        try {
            List<Item> items = itemService.loadItems();
            Optional<Item> item = items.stream()
                    .filter(i -> i.getId().equals(id))
                    .findFirst();

            return item.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Add a new item
    @PostMapping
    public ResponseEntity<String> addItem(@RequestParam String type,
                                          @RequestParam String id,
                                          @RequestParam String name,
                                          @RequestParam int quantity,
                                          @RequestParam(required = false) String expiryDate) {
        try {
            
            // Create item object
            Item newItem;
            if ("P".equalsIgnoreCase(type)) {
                newItem = new PerishableItem(id, name, quantity, expiryDate);
            } else {
                newItem = new NonPerishableItem(id, name, quantity);
            }

            itemService.addItem(newItem);
            return ResponseEntity.ok("Item added successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error adding item: " + e.getMessage());
        }
    }

    // Update an existing item
    @PutMapping("/{id}")
    public ResponseEntity<String> updateItem(@PathVariable String id,
                                             @RequestParam int quantity,
                                             @RequestParam(required = false) String expiryDate) {
        try {
            itemService.updateItem(id, quantity, expiryDate);
            return ResponseEntity.ok("Item updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error updating item: " + e.getMessage());
        }
    }

    // Delete an item by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable String id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.ok("Item deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error deleting item: " + e.getMessage());
        }
    }
}
