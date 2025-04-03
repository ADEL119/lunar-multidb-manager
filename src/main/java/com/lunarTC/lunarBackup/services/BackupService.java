package com.lunarTC.lunarBackup.services;

import com.lunarTC.lunarBackup.utils.DatabaseUtils;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

@Service
public class BackupService {

    public void backupDatabase(DatabaseConfig config, String frequency) {
        try {
            String backupDirectoryPath = getBackupDirectoryPath(frequency, config);
            File backupDir = new File(backupDirectoryPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String backupFilePath = Paths.get(backupDirectoryPath, config.getDatabaseName() + "_" + date).toString();

            ProcessBuilder processBuilder;

            switch (config.getType().toLowerCase()) {
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
                            "--db", config.getDatabaseName(),
                            "--out", backupDirectoryPath
                    );
                    break;
                }

                default:
                    System.out.println("Unsupported database type: " + config.getType());
                    return;
            }

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Backup successful: " + backupFilePath);
            } else {
                System.out.println("Backup failed for: " + config.getDatabaseName());
            }

        } catch ( Exception e) {
            System.err.println("Error while performing backup: " + e.getMessage());
        }
    }

    private String getBackupDirectoryPath(String frequency, DatabaseConfig config) {
        String basePath = config.getBackupPath();
        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);

        return switch (frequency.toLowerCase()) {
            case "daily" -> {
                SimpleDateFormat sdf = new SimpleDateFormat("u");
                int day = Integer.parseInt(sdf.format(now));
                String dayName = switch (day) {
                    case 1 -> "1-Lundi";
                    case 2 -> "2-Mardi";
                    case 3 -> "3-Mercredi";
                    case 4 -> "4-Jeudi";
                    case 5 -> "5-Vendredi";
                    case 6 -> "6-Samedi";
                    case 7 -> "7-Dimanche";
                    default -> throw new IllegalArgumentException("Invalid day number");
                };
                yield Paths.get(basePath, "Daily", dayName).toString();
            }
            case "weekly" -> {
                int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
                yield Paths.get(basePath, "Weekly", "Week" + weekOfMonth).toString();
            }
            case "monthly" -> {
                int month = calendar.get(Calendar.MONTH) + 1;
                String monthName = switch (month) {
                    case 1 -> "1-Janvier";
                    case 2 -> "2-Février";
                    case 3 -> "3-Mars";
                    case 4 -> "4-Avril";
                    case 5 -> "5-Mai";
                    case 6 -> "6-Juin";
                    case 7 -> "7-Juillet";
                    case 8 -> "8-Août";
                    case 9 -> "9-Septembre";
                    case 10 -> "10-Octobre";
                    case 11 -> "11-Novembre";
                    case 12 -> "12-Décembre";
                    default -> throw new IllegalArgumentException("Invalid month number");
                };
                yield Paths.get(basePath, "Monthly", monthName).toString();
            }
            default -> throw new IllegalArgumentException("Unknown frequency: " + frequency);
        };
    }
}
