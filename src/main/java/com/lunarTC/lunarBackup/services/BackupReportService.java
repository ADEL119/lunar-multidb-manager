package com.lunarTC.lunarBackup.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lunarTC.lunarBackup.models.BackupReport;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BackupReportService {

    private final List<BackupReport> reports = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final File reportFile = new File("backup_reports.txt");

    @PostConstruct
    public void init() {
        mapper.registerModule(new JavaTimeModule());
        loadReportsFromFile();
    }

    public void addReport(BackupReport report) {
        reports.add(report);
        saveReportsToFile();  // persist after each addition
    }

    public List<BackupReport> getAllReports() {
        return List.copyOf(reports); // return read-only copy
    }

    private void saveReportsToFile() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(reportFile, reports);
        } catch (IOException e) {
            System.err.println("Failed to save reports: " + e.getMessage());
        }
    }

    private void loadReportsFromFile() {
        if (reportFile.exists()) {
            try {
                List<BackupReport> loaded = mapper.readValue(reportFile, new TypeReference<>() {});
                reports.addAll(loaded);
            } catch (IOException e) {
                System.err.println("Failed to load backup reports: " + e.getMessage());
            }
        }
    }
}
