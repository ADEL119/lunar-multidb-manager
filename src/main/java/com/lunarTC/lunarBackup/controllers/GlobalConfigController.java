package com.lunarTC.lunarBackup.controllers;

import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/config/update")
public class GlobalConfigController {

    @Autowired
    private GlobalConfigLoader configLoader;

    // 🔹 GET full config
    @GetMapping
    public GlobalConfig getConfig() {
        return configLoader.loadGlobalConfig();
    }

    // 🔹 POST full config (save/update)
    @PostMapping
    public void updateConfig(@RequestBody GlobalConfig config) {
        configLoader.saveGlobalConfig(config);
    }

    // 🔹 Optional: check if config exists (for UI logic)
    @GetMapping("/exists")
    public boolean configExists() {
        return configLoader.configExists();
    }

    // 🔥 ADD: POST a single DatabaseConfig to the config file
    @PostMapping("/add-database")
    public GlobalConfig addDatabase(@RequestBody DatabaseConfig newDb) {
        GlobalConfig config = configLoader.loadGlobalConfig();

        if (config.getDatabaseConfigList() == null) {
            config.setDatabaseConfigList(new ArrayList<>());
        }

        config.getDatabaseConfigList().add(newDb);
        configLoader.saveGlobalConfig(config);

        return config;
    }

    // Add this to your GlobalConfigController.java

    @PostMapping("/check-database-exists")
    public boolean checkDatabaseExists(@RequestBody DatabaseConfig dbToCheck) {
        GlobalConfig config = configLoader.loadGlobalConfig();

        if (config.getDatabaseConfigList() == null || config.getDatabaseConfigList().isEmpty()) {
            return false;
        }

        return config.getDatabaseConfigList().stream().anyMatch(existingDb -> {
            // Short name match (safe null checks)
            boolean shortNameMatch = safeEqualsIgnoreCase(existingDb.getShortName(), dbToCheck.getShortName());

            // Connection match (host, port, database)
            boolean connectionMatch =
                    safeEqualsIgnoreCase(existingDb.getHost(), dbToCheck.getHost()) &&
                            Objects.equals(existingDb.getPort(), dbToCheck.getPort()) &&
                            safeEqualsIgnoreCase(existingDb.getDatabase(), dbToCheck.getDatabase());

            return shortNameMatch || connectionMatch;
        });
    }
    private boolean safeEqualsIgnoreCase(String s1, String s2) {
        return s1 != null && s2 != null && s1.equalsIgnoreCase(s2);
    }


}
