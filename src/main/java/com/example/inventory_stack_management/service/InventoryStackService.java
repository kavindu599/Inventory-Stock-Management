package com.example.inventory_stack_management.service;

import com.example.inventory_stack_management.dto.ItemDto;
import com.example.inventory_stack_management.util.FileBackedStringStack; // Import the new stack class
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class InventoryStackService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryStackService.class);
    private static final String ITEMS_DATA_FILE_PATH = "inventory_data.json";
    private static final String STACK_DATA_FILE_PATH = "inventory_stack.txt"; // Path for the stack file

    private final List<ItemDto> inventoryItems = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FileBackedStringStack simpleFileStack; // Instance of our dedicated stack class

    public InventoryStackService() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.simpleFileStack = new FileBackedStringStack(STACK_DATA_FILE_PATH); // Initialize here
    }

    @PostConstruct
    private void initializeData() {
        loadItemsFromFile();
        this.simpleFileStack.loadFromFile(); // loading to the stack class
    }

    @PreDestroy
    private void saveDataOnShutdown() {
        saveItemsToFile();
        this.simpleFileStack.saveToFile(); // saving to the stack class
    }

    private synchronized void loadItemsFromFile() {
        File file = new File(ITEMS_DATA_FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            logger.info("Inventory data file '{}' not found or empty. Starting with an empty inventory.", ITEMS_DATA_FILE_PATH);
            if (inventoryItems.isEmpty() && file.length() == 0) {
                try {
                    ItemDto initialLaptop = new ItemDto("#05673", "Laptop", 3, new java.math.BigDecimal("128"), "Electronics", "null");
                    inventoryItems.add(initialLaptop);
                    logger.info("Initialized with a default Laptop item as inventory_data.json was empty.");
                    saveItemsToFile();
                } catch (Exception e) {
                    logger.error("Error initializing default item: {}", e.getMessage(), e);
                }
            }
            return;
        }
        try {
            List<ItemDto> loadedItems = objectMapper.readValue(file, new TypeReference<List<ItemDto>>() {});
            inventoryItems.clear();
            inventoryItems.addAll(loadedItems);
            logger.info("Loaded {} items from '{}'", inventoryItems.size(), ITEMS_DATA_FILE_PATH);
        } catch (IOException e) {
            logger.error("Error loading inventory from file '{}': {}", ITEMS_DATA_FILE_PATH, e.getMessage(), e);
        }
    }

    private synchronized void saveItemsToFile() {
        try {
            objectMapper.writeValue(new File(ITEMS_DATA_FILE_PATH), inventoryItems);
            logger.info("Saved {} items to '{}'", inventoryItems.size(), ITEMS_DATA_FILE_PATH);
        } catch (IOException e) {
            logger.error("Error saving inventory to file '{}': {}", ITEMS_DATA_FILE_PATH, e.getMessage(), e);
        }
    }

    public List<ItemDto> getAllItems() {
        return new ArrayList<>(inventoryItems);
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


    public void pushSimpleStackItem(String itemName) {
        this.simpleFileStack.push(itemName);
        this.simpleFileStack.saveToFile(); // Save after modification
    }

    public Optional<String> popSimpleStackItem() {
        String poppedItem = this.simpleFileStack.pop();
        if (poppedItem != null) {
            this.simpleFileStack.saveToFile(); // Save after modification
            return Optional.of(poppedItem);
        }
        return Optional.empty();
    }

    public List<String> getSimpleStackItems() {
        return this.simpleFileStack.getItemsView();
    }
}
