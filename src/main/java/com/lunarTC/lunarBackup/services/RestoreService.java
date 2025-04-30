package com.lunarTC.lunarBackup.services;

import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.utils.DatabaseUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class RestoreService {

    public Boolean restoreDatabase(DatabaseConfig config, String backupSource) {
        try {
            String databaseType = config.getType().toLowerCase();
            ProcessBuilder processBuilder;

            switch (databaseType) {
                case "mysql":
                case "mariadb": {
                    String mysql = DatabaseUtils.getCachedRestorePath("mysql");
                    processBuilder = new ProcessBuilder(
                            mysql,
                            "-h", config.getHost(),
                            "-P", String.valueOf(config.getPort()),
                            "-u", config.getUsername(),
                            "-p" + config.getPassword(),
                            config.getDatabaseName()
                    );
                    processBuilder.redirectInput(new File(backupSource));
                    break;
                }

                case "postgres": {
                    String pgRestore = DatabaseUtils.getCachedRestorePath("pg_restore");
                    processBuilder = new ProcessBuilder(
                            pgRestore,
                            "-h", config.getHost(),
                            "-p", String.valueOf(config.getPort()),
                            "-U", config.getUsername(),
                            "-d", config.getDatabaseName(),
                            "-c",  // Clean before restore
                            backupSource
                    );
                    processBuilder.environment().put("PGPASSWORD", config.getPassword());
                    break;
                }

                case "mongo": {
                    String mongoRestore = DatabaseUtils.getCachedRestorePath("mongorestore");
                    processBuilder = new ProcessBuilder(
                            mongoRestore,
                            "--host", config.getHost(),
                            "--port", String.valueOf(config.getPort()),
                            "-u", config.getUsername(),
                            "-p", config.getPassword(),
                            "--authenticationDatabase", config.getAuthenticationDatabase(),
                            "--db", config.getDatabaseName(),
                            "--drop",
                            backupSource
                    );
                    break;
                }

                default:
                    System.out.println("Unsupported database type for restore: " + config.getType());
                    return false;
            }

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Restore Log] " + line);
            }

            int exitCode = process.waitFor();
            System.out.println("Restore process exited with code: " + exitCode);

            return exitCode == 0;

        } catch (Exception e) {
            System.err.println("Error during restore: " + e.getMessage());
            return false;
        }
    }
}
