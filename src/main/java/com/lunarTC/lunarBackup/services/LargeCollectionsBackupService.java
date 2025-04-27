package com.lunarTC.lunarBackup.services;

import com.lunarTC.lunarBackup.models.BackupReport;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.utils.DatabaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class LargeCollectionsBackupService {

    @Autowired
    BackupReportService backupReportService;


    // Replace this value later with a dynamic one based on last backup
    private static final String MIN_OBJECT_ID = "6774776b0000000000000000";

    public boolean backupLargeCollections(DatabaseConfig config, String backupType) {
        try {
            LocalDateTime timestamp = LocalDateTime.now();

            List<String> largeCollections = config.getLargeCollections();
            if (largeCollections == null || largeCollections.isEmpty()) {
                System.out.println("No large collections to back up for database: " + config.getDatabaseName());
                return true;
            }

            String backupDirectoryPath = DatabaseUtils.getBackupDirectoryPath(config, backupType);
            File backupDir = new File(backupDirectoryPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String mongoDump = DatabaseUtils.getCachedDumpPath("mongodump");

            boolean allSucceeded = true; // Track failures

            for (String collection : largeCollections) {
                List<String> command = new ArrayList<>();
                command.add(mongoDump);
                command.add("--host"); command.add(config.getHost());
                command.add("--port"); command.add(String.valueOf(config.getPort()));
                command.add("-u"); command.add(config.getUsername());
                command.add("-p"); command.add(config.getPassword());
                command.add("--authenticationDatabase"); command.add(config.getAuthenticationDatabase());
                command.add("--db"); command.add(config.getDatabaseName());
                command.add("--collection"); command.add(collection);
                //command.add("--query"); command.add("{ \"_id\": { \"$gte\": { \"$oid\": \"" + MIN_OBJECT_ID + "\" } } }");
                command.add("--out"); command.add(backupDirectoryPath);

                System.out.println("Executing mongodump for collection: " + collection);
                System.out.println("Command: " + String.join(" ", command));

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("✅ Backup succeeded for collection: " + collection);
                    backupReportService.addReport(new BackupReport(config.getDatabaseName(), config.getType(), backupType, backupDirectoryPath, timestamp,"SUCCESS"));

                } else {
                    System.err.println("❌ Backup failed for collection: " + collection);
                    backupReportService.addReport(new BackupReport(config.getDatabaseName(), config.getType(), backupType, "N/A", timestamp,"FAILED"));

                    return false;
                }

                Thread.sleep(3000);
            }

            return allSucceeded;

        } catch (Exception e) {
            System.err.println("🔥 Error during large collections backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
