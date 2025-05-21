package com.example.admintoolsandreporting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
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
import java.util.stream.Stream;

@Service
public class ReportFileService {

    private final Path reportsDirPath;
    private static final DateTimeFormatter REPORT_FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public ReportFileService(@Value("${data.reports.dir}") String reportsDirString) {
        this.reportsDirPath = Paths.get(reportsDirString);
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            if (!Files.exists(reportsDirPath)) {
                Files.createDirectories(reportsDirPath);
            }
        } catch (IOException e) {
            System.err.println("Error creating reports directory: " + e.getMessage());
        }
    }

    public String saveReport(String reportType, String content) {
        String timestamp = LocalDateTime.now().format(REPORT_FILENAME_FORMATTER);
        String fileName = reportType + "_" + timestamp + ".txt";
        Path reportFilePath = reportsDirPath.resolve(fileName);
        try {
            Files.writeString(reportFilePath, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            return fileName;
        } catch (IOException e) {
            System.err.println("Error saving report file " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    public List<String> listReportFiles() {
        try (Stream<Path> stream = Files.list(reportsDirPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".txt")) // Ensure it's a text report
                    .sorted(Collections.reverseOrder()) // Show newest first
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error listing report files: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Resource loadReportAsResource(String filename) {
        try {
            Path filePath = reportsDirPath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public String readReportContent(String filename) {
        try {
            Path filePath = reportsDirPath.resolve(filename).normalize();
            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                return Files.readString(filePath, StandardCharsets.UTF_8);
            }
            return "Report not found or not readable.";
        } catch (IOException e) {
            System.err.println("Error reading report content " + filename + ": " + e.getMessage());
            return "Error reading report: " + e.getMessage();
        }
    }
}
