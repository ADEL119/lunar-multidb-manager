package com.lunarTC.lunarBackup.controllers;

import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
}
