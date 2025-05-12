package com.lunarTC.lunarBackup.services;

import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.BackupReport;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import com.lunarTC.lunarBackup.utils.DatabaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LargeCollectionsBackupService {

    @Autowired
    BackupReportService backupReportService;

    @Autowired
    private GlobalConfigLoader globalConfigLoader;

    @Autowired
    private MailService mailService;

    private static final String MIN_OBJECT_ID = "6774776b0000000000000000"; // Optional for future filtering

    public boolean backupLargeCollections(DatabaseConfig config, String backupType) {
        try {
            GlobalConfig globalConfig = globalConfigLoader.loadGlobalConfig();
            LocalDateTime timestamp = LocalDateTime.now();

            List<String> largeCollections = config.getLargeCollections();
            if (largeCollections == null || largeCollections.isEmpty()) {
                System.out.println("No large collections to back up for database: " + config.getDatabase());
                return true;
            }

            String backupDirectoryPath = DatabaseUtils.getBackupDirectoryPath(globalConfig, config, backupType);
            File backupDir = new File(backupDirectoryPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String mongoDump = DatabaseUtils.getCachedDumpPath("mongodump");

            boolean allSucceeded = true;

            for (String collection : largeCollections) {
                List<String> command = new ArrayList<>();
                command.add(mongoDump);
                command.add("--host");
                command.add(config.getHost());
                command.add("--port");
                command.add(String.valueOf(config.getPort()));
                command.add("-u");
                command.add(config.getUsername());
                command.add("-p");
                command.add(config.getPassword());
                command.add("--authenticationDatabase");
                command.add(config.getAuthenticationDatabase());
                command.add("--db");
                command.add(config.getDatabase());
                command.add("--collection");
                command.add(collection);
                command.add("--out");
                command.add(backupDirectoryPath);

                System.out.println("Executing mongodump for collection: " + collection);

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = processBuilder.start();

                BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                List<String> lastErrorLines = new ArrayList<>();

                Thread stdOutReader = new Thread(() -> {
                    String line;
                    try {
                        while ((line = stdOut.readLine()) != null) {
                            System.out.println("[STDOUT] " + line);
                            if (lastErrorLines.size() >= 2) {
                                lastErrorLines.remove(0);
                            }
                            lastErrorLines.add(line);
                        }
                    } catch (Exception e) {
                        System.err.println("Error reading stdout: " + e.getMessage());
                    }
                });

                Thread stdErrReader = new Thread(() -> {
                    String line;
                    try {
                        while ((line = stdErr.readLine()) != null) {
                            System.err.println("[STDERR] " + line);
                        }
                    } catch (Exception e) {
                        System.err.println("Error reading stderr: " + e.getMessage());
                    }
                });

                stdOutReader.start();
                stdErrReader.start();

                int exitCode = process.waitFor();
                stdOutReader.join();
                stdErrReader.join();

                if (exitCode == 0) {
                    System.out.println("✅ Backup succeeded for collection: " + collection);
                    backupReportService.addReport(new BackupReport(config.getDatabase(), config.getType(), backupType, backupDirectoryPath, timestamp, "SUCCESS"));
                } else {
                    System.err.println("❌ Backup failed for collection: " + collection);
                    backupReportService.addReport(new BackupReport(config.getDatabase(), config.getType(), backupType, "N/A", timestamp, "FAILED"));

                    try {
                        String errorSummary = String.join("\n", lastErrorLines);
                        String errorBody = mailService.buildBackupFailureEmail(
                                config.getDatabase(),
                                config.getType(),
                                backupType,
                                errorSummary.isEmpty() ? "Unknown error during mongodump." : errorSummary
                        );

                        String subject = "❌ Failed : " + config.getDatabase() + " : " + backupType;

                        for (String emailTo : config.getEmailList()) {
                            mailService.sendHtmlEmail(emailTo, subject, errorBody);
                        }
                    } catch (Exception e) {
                        System.out.println("Mail failed: " + e.getMessage());
                    }

                    allSucceeded = false;
                }

                Thread.sleep(3000); // Optional: pause between collections
            }

            if (allSucceeded) {
                try {
                    String html = mailService.buildBackupSuccessEmail(config.getDatabase(), config.getType(), backupType, backupDirectoryPath);
                    String subject = "✅ Successful : " + config.getDatabase() + " : " + backupType;
                    for (String emailTo : config.getEmailList()) {
                        mailService.sendHtmlEmail(emailTo, subject, html);
                    }
                } catch (Exception e) {
                    System.out.println("Mail failed: " + e.getMessage());
                }
            }

            return allSucceeded;

        } catch (Exception e) {
            System.err.println("Error during large collections backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
