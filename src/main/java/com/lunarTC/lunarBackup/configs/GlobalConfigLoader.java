package com.lunarTC.lunarBackup.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Service
public class GlobalConfigLoader {

    public GlobalConfig loadGlobalConfig() {
        try {
            File jarDir = new File(Paths.get("").toAbsolutePath().toString());
            File configFile = new File(jarDir, "config.json");

            if (!configFile.exists()) {
                throw new RuntimeException("Configuration file not found: " + configFile.getAbsolutePath());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(configFile,GlobalConfig.class);

        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration file", e);
        }
    }
}
