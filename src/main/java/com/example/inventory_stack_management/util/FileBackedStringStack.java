package com.example.inventory_stack_management.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class FileBackedStringStack {
    private static final Logger logger = LoggerFactory.getLogger(FileBackedStringStack.class);
    private final String filePath;
    private final Deque<String> stack;

    public FileBackedStringStack(String filePath) {
        this.filePath = filePath;
        this.stack = new ArrayDeque<>();
    }

    // --- Stack Operations (Abstraction) ---
    public synchronized void push(String item) {
        if (item == null || item.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name to push cannot be null or empty.");
        }
        this.stack.push(item); // Pushes onto the top of the stack
        logger.info("Pushed '{}' onto stack. Current size: {}", item, this.stack.size());
    }

    public synchronized String pop() {
        if (this.stack.isEmpty()) {
            logger.warn("Attempted to pop from an empty stack.");
            return null;
        }
        String item = this.stack.pop(); // Removes and returns the top item
        logger.info("Popped '{}' from stack. Current size: {}", item, this.stack.size());
        return item;
    }

    public synchronized String peek() {
        if (this.stack.isEmpty()) {
            return null;
        }
        return this.stack.peek(); // Returns the top item without removing it
    }

    public synchronized boolean isEmpty() {
        return this.stack.isEmpty();
    }

    public synchronized int size() {
        return this.stack.size();
    }

    public synchronized List<String> getItemsView() {
        return new ArrayList<>(this.stack);
    }

    // --- File Handling (Encapsulated) ---
    public synchronized void loadFromFile() {
        File file = new File(this.filePath);
        this.stack.clear(); // Start fresh

        if (!file.exists()) {
            logger.info("Stack data file '{}' not found. Starting with an empty stack.", this.filePath);
            // Attempt to create it so it exists for future saves
            try {
                if (file.createNewFile()) {
                    logger.info("Created empty stack file: {}", this.filePath);
                }
            } catch (IOException e) {
                logger.error("Could not create stack file '{}': {}", this.filePath, e.getMessage(), e);
            }
            return;
        }

        if (file.length() == 0) {
            logger.info("Stack data file '{}' is empty. Starting with an empty stack.", this.filePath);
            return;
        }

        List<String> linesInFileOrder = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    linesInFileOrder.add(line.trim());
                }
            }
        } catch (IOException e) {
            logger.error("Error loading stack from file '{}': {}. Stack remains empty.", this.filePath, e.getMessage(), e);
            this.stack.clear(); // Ensure stack is clear if loading fails
            return;
        }

        Collections.reverse(linesInFileOrder);
        for (String item : linesInFileOrder) {
            this.stack.push(item); // This adds to the "top" of our internal Deque
        }
        logger.info("Loaded {} items into stack from '{}'. Top item: '{}'", this.stack.size(), this.filePath, this.stack.peek());
    }

    public synchronized void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath, false))) { // false to overwrite
            for (String itemName : this.stack) {
                writer.write(itemName);
                writer.newLine();
            }
            logger.info("Saved {} items from stack to file '{}'", this.stack.size(), this.filePath);
        } catch (IOException e) {
            logger.error("Error saving stack to file '{}': {}", this.filePath, e.getMessage(), e);
        }
    }
}
