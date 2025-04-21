package com.lunarTC.lunarBackup.services;

import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.utils.DatabaseUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LargeCollectionsBackupService {

    public boolean backupLargeCollections(DatabaseConfig config, String backupType) {

        try {
            List<String> largeCollections = config.getLargeCollections();
            String backupDirectoryPath = DatabaseUtils.getBackupDirectoryPath(config, backupType);
            File backupDir = new File(backupDirectoryPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String mongoDump = DatabaseUtils.getCachedDumpPath("mongodump");

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
                command.add("--query"); command.add("{\"createdAt\": { \"$gte\": { \"$date\": \"" + getTodayIsoDate() + "\" } } }");
                command.add("--out"); command.add(backupDirectoryPath);

                ProcessBuilder processBuilder = new ProcessBuilder(command);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private String getTodayIsoDate() {
        return java.time.LocalDate.now().toString() + "T00:00:00Z";
    }
}
