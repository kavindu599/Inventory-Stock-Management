package com.example.inventory_stack_management.service;

import com.example.inventory_stack_management.dto.ItemDto;
import com.fasterxml.jackson.core.type.TypeReference; // For Jackson
import com.fasterxml.jackson.databind.ObjectMapper;     // For Jackson
import com.fasterxml.jackson.databind.SerializationFeature; // For pretty printing JSON
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList; // For thread-safety if needed, or synchronize methods

@Service
public class InventoryStackService { // Consider renaming to InventoryItemService later

    private static final Logger logger = LoggerFactory.getLogger(InventoryStackService.class);
    // Change file name and format
    private static final String DATA_FILE_PATH = "inventory_data.json";

    // Change to a List to store richer items
    private final List<ItemDto> inventoryItems = new CopyOnWriteArrayList<>(); // Thread-safe list
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InventoryStackService() {
        
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    
    }

    @PostConstruct
    private synchronized void loadItemsFromFile() {
        File file = new File(DATA_FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            logger.info("Inventory data file '{}' not found or empty. Starting with an empty inventory.", DATA_FILE_PATH);
            return;
        }
        try {
            List<ItemDto> loadedItems = objectMapper.readValue(file, new TypeReference<List<ItemDto>>() {});
            inventoryItems.addAll(loadedItems);
            logger.info("Loaded {} items from '{}'", inventoryItems.size(), DATA_FILE_PATH);
        } catch (IOException e) {
            logger.error("Error loading inventory from file '{}': {}", DATA_FILE_PATH, e.getMessage(), e);
        }
    }

    @PreDestroy
    private synchronized void saveItemsToFile() {
        try {
            objectMapper.writeValue(new File(DATA_FILE_PATH), inventoryItems);
            logger.info("Saved {} items to '{}'", inventoryItems.size(), DATA_FILE_PATH);
        } catch (IOException e) {
            logger.error("Error saving inventory to file '{}': {}", DATA_FILE_PATH, e.getMessage(), e);
        }
    }

    // --- CRUD Operations ---

    public List<ItemDto> getAllItems() {
        return new ArrayList<>(inventoryItems); // Return a copy
    }

    public Optional<ItemDto> getItemByCode(String itemCode) {
        return inventoryItems.stream()
                .filter(item -> item.getItemCode().equalsIgnoreCase(itemCode))
                .findFirst();
    }

    public synchronized ItemDto addItem(ItemDto newItem) {
        if (newItem == null || newItem.getItemCode() == null || newItem.getItemCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Item and Item Code cannot be null or empty.");
        }
        if (getItemByCode(newItem.getItemCode()).isPresent()) {
            throw new IllegalArgumentException("Item with code '" + newItem.getItemCode() + "' already exists.");
        }
        inventoryItems.add(newItem);
        logger.info("Added item: {}", newItem.getItemCode());
        saveItemsToFile();
        return newItem;
    }

    public synchronized Optional<ItemDto> updateItem(String itemCode, ItemDto updatedItemDetails) {
        Optional<ItemDto> existingItemOpt = getItemByCode(itemCode);
        if (existingItemOpt.isPresent()) {
            ItemDto existingItem = existingItemOpt.get();
            // Update fields - be careful if itemCode can be updated
            existingItem.setName(updatedItemDetails.getName());
            existingItem.setQuantity(updatedItemDetails.getQuantity());
            existingItem.setUnitPrice(updatedItemDetails.getUnitPrice());
            existingItem.setCategory(updatedItemDetails.getCategory());
            existingItem.setDescription(updatedItemDetails.getDescription());

            logger.info("Updated item: {}", itemCode);
            saveItemsToFile();
            return Optional.of(existingItem);
        }
        logger.warn("Attempted to update non-existent item: {}", itemCode);
        return Optional.empty();
    }

    public synchronized boolean deleteItemByCode(String itemCode) {
        boolean removed = inventoryItems.removeIf(item -> item.getItemCode().equalsIgnoreCase(itemCode));
        if (removed) {
            logger.info("Deleted item: {}", itemCode);
            saveItemsToFile();
        } else {
            logger.warn("Attempted to delete non-existent item: {}", itemCode);
        }
        return removed;
    }

    // --- Keep simplified LIFO-like methods if still needed for a part of your app,
    //     or remove them if the new UI makes them obsolete.
    //     For now, I'm commenting them out to focus on the new structure.

    /*
    public synchronized void pushSimpleItem(String itemName) { // Example, if you still need a simple push
        // This would need a way to create a full ItemDto or a different handling
        ItemDto simplePushItem = new ItemDto("TEMP_CODE_" + System.currentTimeMillis(), itemName, 1, BigDecimal.ZERO, "Default", "Simple push");
        inventoryItems.add(simplePushItem); // Adds to the end, like a queue. For stack, add to beginning or use Deque.
        saveItemsToFile();
    }

    public synchronized Optional<ItemDto> popSimpleItem() { // Example
        if (inventoryItems.isEmpty()) {
            return Optional.empty();
        }
        ItemDto popped = inventoryItems.remove(inventoryItems.size() - 1); // LIFO from end of list
        saveItemsToFile();
        return Optional.of(popped);
    }
    */
}
