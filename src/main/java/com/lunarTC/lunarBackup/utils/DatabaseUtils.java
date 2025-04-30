package com.lunarTC.lunarBackup.utils;

import com.lunarTC.lunarBackup.models.DatabaseConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatabaseUtils {

    private static String mysqlDumpPath;
    private static String postgresDumpPath;
    private static String mongoDumpPath;

    private static String mysqlRestorePath;
    private static String postgresRestorePath;
    private static String mongoRestorePath;

    public static String getToolPath(String tool) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        String command = os.contains("win") ? "where " + tool : "which " + tool;

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            return (line != null) ? line.trim() : null;
        }
    }

    public static String getCachedDumpPath(String tool) throws Exception {
        return switch (tool.toLowerCase()) {
            case "mysqldump" -> {
                if (mysqlDumpPath == null) mysqlDumpPath = getToolPath(tool);
                yield mysqlDumpPath;
            }
            case "pg_dump" -> {
                if (postgresDumpPath == null) postgresDumpPath = getToolPath(tool);
                yield postgresDumpPath;
            }
            case "mongodump" -> {
                if (mongoDumpPath == null) mongoDumpPath = getToolPath(tool);
                yield mongoDumpPath;
            }
            default -> throw new IllegalArgumentException("Unknown dump tool: " + tool);
        };
    }

    public static String getCachedRestorePath(String tool) throws Exception {
        return switch (tool.toLowerCase()) {
            case "mysql" -> {
                if (mysqlRestorePath == null) mysqlRestorePath = getToolPath(tool);
                yield mysqlRestorePath;
            }
            case "pg_restore" -> {
                if (postgresRestorePath == null) postgresRestorePath = getToolPath(tool);
                yield postgresRestorePath;
            }
            case "mongorestore" -> {
                if (mongoRestorePath == null) mongoRestorePath = getToolPath(tool);
                yield mongoRestorePath;
            }
            default -> throw new IllegalArgumentException("Unknown restore tool: " + tool);
        };
    }

    public static String getBackupDirectoryPath(DatabaseConfig config, String frequency) {
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
                yield Paths.get(basePath, config.getDatabaseName(), "Daily", dayName).toString();
            }
            case "weekly" -> {
                int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
                yield Paths.get(basePath, config.getDatabaseName(), "Weekly", "Week" + weekOfMonth).toString();
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
            default -> Paths.get(basePath, config.getDatabaseName(), frequency).toString();
        };
    }
}
