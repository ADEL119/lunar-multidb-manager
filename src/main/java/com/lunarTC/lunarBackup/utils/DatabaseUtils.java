package com.lunarTC.lunarBackup.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DatabaseUtils {

    private static String mysqlDumpPath;
    private static String postgresDumpPath;
    private static String mongoDumpPath;

    public static String getDumpPath(String tool) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        String command = os.contains("win") ? "where " + tool : "which " + tool;

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            if (line != null) {
                return line.trim();
            } else {
                return null;
            }
        }
    }

    public static String getCachedDumpPath(String tool) throws Exception {
        return switch (tool.toLowerCase()) {
            case "mysqldump" -> {
                if (mysqlDumpPath == null) mysqlDumpPath = getDumpPath(tool);
                yield mysqlDumpPath;
            }
            case "pg_dump" -> {
                if (postgresDumpPath == null) postgresDumpPath = getDumpPath(tool);
                yield postgresDumpPath;
            }
            case "mongodump" -> {
                if (mongoDumpPath == null) mongoDumpPath = getDumpPath(tool);
                yield mongoDumpPath;
            }
            default -> throw new IllegalArgumentException("Unknown tool: " + tool);
        };
    }
}
