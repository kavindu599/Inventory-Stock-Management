package com.example.admintoolsandreporting.service;

import com.example.admintoolsandreporting.model.*;
import com.example.admintoolsandreporting.util.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final ItemRepository itemRepository;
    private final ReportFileService reportFileService;
    private final Path logFilePath;

    public AdminService(ItemRepository itemRepository,
                        ReportFileService reportFileService,
                        @Value("${data.logs.path}") String logPathString) {
        this.itemRepository = itemRepository;
        this.reportFileService = reportFileService;
        this.logFilePath = Paths.get(logPathString);
        ensureLogFileExists();
    }

    private void ensureLogFileExists() {
        try {
            if (!Files.exists(logFilePath.getParent())) {
                Files.createDirectories(logFilePath.getParent());
            }
            if (!Files.exists(logFilePath)) {
                Files.createFile(logFilePath);
            }
        } catch (IOException e) {
            System.err.println("Error ensuring log file exists: " + e.getMessage());
        }
    }

    // --- Dashboard Methods ---
    public DashboardStats getDashboardStats() {
        List<Item> items = itemRepository.findAll();
        long totalProducts = items.size();
        long lowStockCount = items.stream().filter(item -> item.getQuantity() > 0 && item.getQuantity() < Constants.LOW_STOCK_THRESHOLD && !item.isExpired()).count();
        double totalValue = items.stream().filter(item -> !item.isExpired()).mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        logAdminAction("Viewed Dashboard Stats");
        return new DashboardStats(totalProducts, lowStockCount, totalValue);
    }

    public List<ItemView> getCurrentStockForDashboard() {
        return itemRepository.findAll().stream()
                .map(ItemView::fromItem)
                .collect(Collectors.toList());
    }

    // --- Reporting Methods (Read Operations) ---
    public ReportStats getReportStats() {
        List<Item> items = itemRepository.findAll();
        long lowStock = items.stream().filter(item -> item.getQuantity() > 0 && item.getQuantity() < Constants.LOW_STOCK_THRESHOLD && !item.isExpired()).count();
        long expiringSoon = items.stream().filter(Item::isExpiringSoon).count();
        long expired = items.stream().filter(Item::isExpired).count();
        logAdminAction("Viewed Report Stats");
        return new ReportStats(lowStock, expiringSoon, expired);
    }

    public List<ItemView> getLowStockItems() {
        logAdminAction("Viewed Low Stock Items List");
        return itemRepository.findAll().stream()
                .filter(item -> item.getQuantity() > 0 && item.getQuantity() < Constants.LOW_STOCK_THRESHOLD && !item.isExpired())
                .map(ItemView::fromItem)
                .collect(Collectors.toList());
    }

    public List<ItemView> getExpiringSoonItems() {
        logAdminAction("Viewed Expiring Soon Items List");
        return itemRepository.findAll().stream()
                .filter(Item::isExpiringSoon)
                .map(ItemView::fromItem)
                .collect(Collectors.toList());
    }

    public List<ItemView> getExpiredItemsForReport() {
        logAdminAction("Viewed Expired Items List for Report");
        return itemRepository.findAll().stream()
                .filter(Item::isExpired)
                .map(ItemView::fromItem)
                .collect(Collectors.toList());
    }

    public String generateLowStockReport() {
        List<Item> lowStockItems = itemRepository.findAll().stream()
                .filter(item -> item.getQuantity() > 0 && item.getQuantity() < Constants.LOW_STOCK_THRESHOLD && !item.isExpired())
                .collect(Collectors.toList());

        StringBuilder reportContent = new StringBuilder("Low Stock Report - Generated on " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
        reportContent.append("----------------------------------------------------------\n");
        reportContent.append(String.format("%-38s %-20s %-10s %-10s\n", "ID", "Name", "Category", "Quantity"));
        reportContent.append("----------------------------------------------------------\n");
        if (lowStockItems.isEmpty()) {
            reportContent.append("No low stock items found.\n");
        } else {
            lowStockItems.forEach(item ->
                    reportContent.append(String.format("%-38s %-20s %-10s %-10d\n", item.getId(), item.getName(), item.getCategory(), item.getQuantity()))
            );
        }
        reportContent.append("----------------------------------------------------------\n");
        String fileName = reportFileService.saveReport("low_stock", reportContent.toString());
        if (fileName != null) logAdminAction("Generated Low Stock Report: " + fileName);
        return fileName;
    }

    public String generateExpiringSoonReport() {
        List<Item> expiringSoonItemsList = itemRepository.findAll().stream()
                .filter(Item::isExpiringSoon)
                .collect(Collectors.toList());

        StringBuilder reportContent = new StringBuilder("Expiring Soon Report - Generated on " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
        reportContent.append("---------------------------------------------------------------------------------\n");
        reportContent.append(String.format("%-38s %-20s %-10s %-10s %-12s\n", "ID", "Name", "Category", "Quantity", "Expiry Date"));
        reportContent.append("---------------------------------------------------------------------------------\n");
        if (expiringSoonItemsList.isEmpty()) {
            reportContent.append("No items expiring soon.\n");
        } else {
            expiringSoonItemsList.forEach(item ->
                    reportContent.append(String.format("%-38s %-20s %-10s %-10d %-12s\n",
                            item.getId(), item.getName(), item.getCategory(), item.getQuantity(),
                            item.getExpiryDate() != null ? item.getExpiryDate().toString() : "N/A"))
            );
        }
        reportContent.append("---------------------------------------------------------------------------------\n");

        String fileName = reportFileService.saveReport("expiring_soon", reportContent.toString());
        if (fileName != null) logAdminAction("Generated Expiring Soon Report: " + fileName);
        return fileName;
    }

    // --- Delete Operation ---
    public int deleteExpiredItemsViaReport() { // "via report" means acting on the category of items that *would* be in an expired report
        List<Item> allItems = itemRepository.findAll();
        List<Item> itemsToKeep = allItems.stream()
                .filter(item -> !item.isExpired())
                .collect(Collectors.toList());

        int deletedCount = allItems.size() - itemsToKeep.size();
        if (deletedCount > 0) {
            itemRepository.saveAll(itemsToKeep);
            logAdminAction("Deleted " + deletedCount + " expired items.");
        } else {
            logAdminAction("Attempted to delete expired items, none found.");
        }
        return deletedCount;
    }

    // --- Logging ---
    public void logAdminAction(String action) {
        // In a real app, you'd get the Admin username from the security context
        String adminUser = Constants.ADMIN_USERNAME; // Placeholder
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String logMessage = timestamp + " | User: " + adminUser + " | Action: " + action + "\n";
        try {
            Files.writeString(logFilePath, logMessage, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error writing to admin log: " + e.getMessage());
        }
    }

    public List<LogEntry> getAdminLogs(int limit) {
        try {
            List<String> allLines = Files.readAllLines(logFilePath, StandardCharsets.UTF_8);
            Collections.reverse(allLines); // Newest first
            return allLines.stream()
                    .limit(limit)
                    .map(line -> {
                        String[] parts = line.split("\\|"); // Split by pipe
                        if (parts.length >= 3) {
                            return new LogEntry(parts[0].trim(), parts[2].replace("Action:", "").trim());
                        }
                        return new LogEntry("N/A", line); // Fallback for malformed lines
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading admin logs: " + e.getMessage());
            return Collections.singletonList(new LogEntry("Error", "Could not load logs."));
        }
    }
}
