package com.lunarTC.lunarBackup.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import com.lunarTC.lunarBackup.models.EmailConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;

@Service
public class GlobalConfigLoader {

    private final File configFile;

    public GlobalConfigLoader() {
        File jarDir = new File(Paths.get("").toAbsolutePath().toString());
        this.configFile = new File(jarDir, "Lunar_DB_Backup_Config.json");

        // Auto-create empty config file on first run
        if (!configFile.exists()) {
            createEmptyConfigFile();
        }
    }

    private void createEmptyConfigFile() {
        try {
            GlobalConfig emptyConfig = new GlobalConfig();
            emptyConfig.setPathDirectory(null);
            emptyConfig.setNotificationConfig(new EmailConfig());
            emptyConfig.setDatabaseConfigList(Collections.emptyList());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, emptyConfig);

            System.out.println("Created empty config file at: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create empty configuration file", e);
        }
    }

    public GlobalConfig loadGlobalConfig() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(configFile, GlobalConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration file", e);
        }
    }

    public void saveGlobalConfig(GlobalConfig config) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
        } catch (IOException e) {
            throw new RuntimeException("Error saving configuration file", e);
        }
    }

    public boolean configExists() {
        return configFile.exists();
    }
}
