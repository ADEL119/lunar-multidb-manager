package com.lunarTC.lunarBackup.services;

import com.lunarTC.lunarBackup.jobs.DynamicBackupJob;
import com.lunarTC.lunarBackup.models.BackupReport;
import com.lunarTC.lunarBackup.utils.DatabaseUtils;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;




@Service
public class BackupService {
    @Autowired
    private BackupReportService backupReportService;


    @Autowired
    private MailService mailService;


    public Boolean backupDatabase(DatabaseConfig config, String frequency) {
        try {
            String backupDirectoryPath = DatabaseUtils.getBackupDirectoryPath(frequency, config);
            File backupDir = new File(backupDirectoryPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            LocalDateTime timestamp = LocalDateTime.now();
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String backupFilePath = Paths.get(backupDirectoryPath, config.getDatabaseName() + "_" + date).toString();

            ProcessBuilder processBuilder;

            switch (config.getType().toLowerCase()){
                case "mysql":
                case "mariadb": {
                    backupFilePath += ".sql";
                    String mysqldump = DatabaseUtils.getCachedDumpPath("mysqldump");
                    processBuilder = new ProcessBuilder(
                            mysqldump,
                            "-h", config.getHost(),
                            "-P", String.valueOf(config.getPort()),
                            "-u", config.getUsername(),
                            "--password=" + config.getPassword(),
                            config.getDatabaseName(),
                            "-r", backupFilePath
                    );
                    break;
                }

                case "postgres": {
                    backupFilePath += ".backup";
                    String pgDump = DatabaseUtils.getCachedDumpPath("pg_dump");
                    processBuilder = new ProcessBuilder(
                            pgDump,
                            "-h", config.getHost(),
                            "-p", String.valueOf(config.getPort()),
                            "-U", config.getUsername(),
                            "-F", "c",
                            "-f", backupFilePath,
                            config.getDatabaseName()
                    );
                    processBuilder.environment().put("PGPASSWORD", config.getPassword());
                    break;
                }

                case "mongo": {
                    String mongoDump = DatabaseUtils.getCachedDumpPath("mongodump");
                    processBuilder = new ProcessBuilder(
                            mongoDump,
                            "--host", config.getHost(),
                            "--port", String.valueOf(config.getPort()),
                            "-u", config.getUsername(),
                            "-p", config.getPassword(),
                            "--authenticationDatabase", config.getAuthenticationDatabase(),
                            "--db", config.getDatabaseName(),
                            "--out", backupDirectoryPath
                    );
                    break;
                }


                default:
                    System.out.println("Unsupported database type: " + config.getType()+". Skipping backup");
                    return true;
            }

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Successful Backup: " +frequency+"   :" +backupFilePath);
                try {
                    String html = mailService.buildBackupSuccessEmail(config.getDatabaseName(), config.getType(), frequency, backupFilePath);
                    mailService.sendHtmlEmail("adelselmi8@gmail.com", "✅ Backup Completed", html);

                } catch(Exception e){
                    System.out.println("Mail failed"+e.getMessage());
                }
                backupReportService.addReport(new BackupReport(config.getDatabaseName(), config.getType(), frequency, backupFilePath, timestamp,"SUCCESS"));
                    return true;
            } else {
                System.out.println("Failed Backup: " +frequency+"   :" + config.getDatabaseName() + " ======> " + config.getType() );
                backupReportService.addReport(new BackupReport(config.getDatabaseName(), config.getType(), frequency, "N/A", timestamp,"FAILED"));
                try {
                    String errorBody = mailService.buildBackupFailureEmail(
                            config.getDatabaseName(),
                            config.getType(),
                            frequency,
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
