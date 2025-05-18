package com.example.admintoolsandreporting.controller;

import com.example.admintoolsandreporting.model.*;
import com.example.admintoolsandreporting.service.AdminService;
import com.example.admintoolsandreporting.service.ReportFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin") // Base path for all admin API endpoints
public class AdminController {

    private final AdminService adminService;
    private final ReportFileService reportFileService;

    public AdminController(AdminService adminService, ReportFileService reportFileService) {
        this.adminService = adminService;
        this.reportFileService = reportFileService;
    }

    // --- Dashboard Endpoints ---
    @GetMapping("/dashboard-stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/stock/current")
    public ResponseEntity<List<ItemView>> getCurrentStock() {
        return ResponseEntity.ok(adminService.getCurrentStockForDashboard());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<LogEntry>> getLogs(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(adminService.getAdminLogs(limit));
    }

    // --- Report Page Endpoints ---
    @GetMapping("/report-stats")
    public ResponseEntity<ReportStats> getReportStats() {
        return ResponseEntity.ok(adminService.getReportStats());
    }

    @GetMapping("/reports/items/low-stock")
    public ResponseEntity<List<ItemView>> getLowStockItemsData() {
        return ResponseEntity.ok(adminService.getLowStockItems());
    }

    @GetMapping("/reports/items/expiring-soon")
    public ResponseEntity<List<ItemView>> getExpiringSoonItemsData() {
        return ResponseEntity.ok(adminService.getExpiringSoonItems());
    }

    @GetMapping("/reports/items/expired")
    public ResponseEntity<List<ItemView>> getExpiredItemsData() {
        return ResponseEntity.ok(adminService.getExpiredItemsForReport());
    }

    @PostMapping("/reports/generate/low-stock")
    public ResponseEntity<Map<String, String>> generateLowStockReport() {
        String fileName = adminService.generateLowStockReport();
        Map<String, String> response = new HashMap<>();
        if (fileName != null) {
            response.put("message", "Low stock report generated successfully: " + fileName);
            response.put("fileName", fileName);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to generate low stock report.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/reports/generate/expiring-soon")
    public ResponseEntity<Map<String, String>> generateExpiringSoonReport() {
        String fileName = adminService.generateExpiringSoonReport();
        Map<String, String> response = new HashMap<>();
        if (fileName != null) {
            response.put("message", "Expiring soon report generated successfully: " + fileName);
            response.put("fileName", fileName);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to generate expiring soon report.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/items/delete-expired")
    public ResponseEntity<Map<String, String>> deleteExpiredItems() {
        int count = adminService.deleteExpiredItemsViaReport();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Deleted " + count + " expired item(s).");
        response.put("deletedCount", String.valueOf(count));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/list")
    public ResponseEntity<List<String>> listGeneratedReports() {
        return ResponseEntity.ok(reportFileService.listReportFiles());
    }

    @GetMapping("/reports/view/{filename:.+}") // :.+ to allow dots in filename
    public ResponseEntity<String> viewReportContent(@PathVariable String filename) {
        String content = reportFileService.readReportContent(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    @GetMapping("/reports/download/{filename:.+}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String filename) {
        Resource resource = reportFileService.loadReportAsResource(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
