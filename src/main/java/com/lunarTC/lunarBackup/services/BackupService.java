package com.lunarTC.lunarBackup.services;


import com.lunarTC.lunarBackup.models.BackupReport;
import com.lunarTC.lunarBackup.utils.DatabaseUtils;
import com.lunarTC.lunarBackup.models.DatabaseConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

import java.nio.file.Paths;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;





@Service
public class BackupService {
    @Autowired
    private BackupReportService backupReportService;


    @Autowired
    private MailService mailService;


    public Boolean backupDatabase(DatabaseConfig config, String backupType) {
        try {
            String backupDirectoryPath = DatabaseUtils.getBackupDirectoryPath(config,backupType);
            File backupDir = new File(backupDirectoryPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
           LocalDateTime timestamp = LocalDateTime.now();
            String backupFileName = Paths.get(backupDirectoryPath, config.getDatabaseName()).toString();



            ProcessBuilder processBuilder;

            switch (config.getType().toLowerCase()){
                case "mysql":
                case "mariadb": {
                    backupFileName += ".sql";
                    String mysqldump = DatabaseUtils.getCachedDumpPath("mysqldump");
                    processBuilder = new ProcessBuilder(
                            mysqldump,
                            "-h", config.getHost(),
                            "-P", String.valueOf(config.getPort()),
                            "-u", config.getUsername(),
                            "--password=" + config.getPassword(),
                            config.getDatabaseName(),
                            "-r", backupFileName
                    );
                    break;
                }

                case "postgres": {
                    backupFileName += ".backup";
                    String pgDump = DatabaseUtils.getCachedDumpPath("pg_dump");
                    processBuilder = new ProcessBuilder(
                            pgDump,
                            "-h", config.getHost(),
                            "-p", String.valueOf(config.getPort()),
                            "-U", config.getUsername(),
                            "-F", "c",
                            "-f", backupFileName,
                            config.getDatabaseName()
                    );
                    processBuilder.environment().put("PGPASSWORD", config.getPassword());
                    break;
                }

                case "mongo": {
                    String mongoDump = DatabaseUtils.getCachedDumpPath("mongodump");

                    // Start building the base command
                    StringBuilder commandBuilder = new StringBuilder();
                    commandBuilder.append(mongoDump).append(" ")
                            .append("--host ").append(config.getHost()).append(" ")
                            .append("--port ").append(config.getPort()).append(" ")
                            .append("-u ").append(config.getUsername()).append(" ")
                            .append("-p ").append(config.getPassword()).append(" ")
                            .append("--authenticationDatabase ").append(config.getAuthenticationDatabase()).append(" ")
                            .append("--db ").append(config.getDatabaseName()).append(" ")
                            .append("--out ").append(backupDirectoryPath).append(" ");

                    // Exclude large collections if any
                    if (config.getLargeCollections() != null && !config.getLargeCollections().isEmpty()) {
                        for (String collection : config.getLargeCollections()) {
                            commandBuilder.append("--excludeCollection=").append(collection).append(" ");
                        }
                    }

                    // Convert to command array for ProcessBuilder
                    String[] command = commandBuilder.toString().trim().split("\\s+");

                    processBuilder = new ProcessBuilder(command);
                    break;
                }


                default:
                    System.out.println("Unsupported database backupType: " + config.getType()+". Skipping backup");
                    return true;
            }

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Successful Backup: " +backupType+"   :" +config.getType());
                try {
                    String html = mailService.buildBackupSuccessEmail(config.getDatabaseName(), config.getType(), backupType, backupFileName);
                    mailService.sendHtmlEmail("adelselmi8@gmail.com", "✅ Backup Completed", html);

                } catch(Exception e){
                    System.out.println("Mail failed"+e.getMessage());
                }
                backupReportService.addReport(new BackupReport(config.getDatabaseName(), config.getType(), backupType, backupFileName, timestamp,"SUCCESS"));
                    return true;
            } else {
                System.out.println("Failed Backup: " +backupType+"   :" + config.getDatabaseName() + " ======> " + config.getType() );
                backupReportService.addReport(new BackupReport(config.getDatabaseName(), config.getType(), backupType, "N/A", timestamp,"FAILED"));
                try {
                    String errorBody = mailService.buildBackupFailureEmail(
                            config.getDatabaseName(),
                            config.getType(),
                            backupType,
                            "Exit code != 0 or dump process failed."
                    );

                    mailService.sendHtmlEmail("adelselmi8@gmail.com", "❌ Backup Failed", errorBody);

                }
                catch (Exception e)
                {
                    System.out.println("Mail failed"+e.getMessage());
                }
                return false;
                }

        } catch ( Exception e) {
            System.err.println("Error while performing backup: " + e.getMessage());
        }
        return false;
    }




}
