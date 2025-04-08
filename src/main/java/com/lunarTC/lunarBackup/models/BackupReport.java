package com.lunarTC.lunarBackup.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BackupReport {
    private String databaseName;
    private String type;          // mongo, mysql, etc.
    private String frequency;     // manual, daily, etc.
    private String filePath;
    private LocalDateTime timestamp;
    private String status;        // SUCCESS / FAILED

    // Required by Jackson for reading from JSON
    public BackupReport() {}

    public BackupReport(String databaseName, String type, String frequency, String filePath, LocalDateTime timestamp, String status) {
        this.databaseName = databaseName;
        this.type = type;
        this.frequency = frequency;
        this.filePath = filePath;
        this.timestamp = timestamp;
        this.status = status;
    }
}
