package com.lunarTC.lunarBackup.controllers;


import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import com.lunarTC.lunarBackup.services.RestoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restore")
public class RestoreController {

    @Autowired
    GlobalConfigLoader globalConfigLoader;

    @Autowired
    RestoreService restoreService;

    @GetMapping("/{dbName}/{dbType}")
    public ResponseEntity<String> restoreDatabase(@PathVariable String dbName, @PathVariable String dbType, @RequestParam String dataSource) {

        GlobalConfig globalConfig = globalConfigLoader.loadGlobalConfig();

        List<DatabaseConfig> databaseConfigs = globalConfig.getDatabaseConfigList();

        for (DatabaseConfig dbConfig : databaseConfigs) {

            if (dbConfig.getDatabase().equalsIgnoreCase(dbName) && dbConfig.getType().equalsIgnoreCase(dbType)) {

                boolean restoreSucceeded = restoreService.restoreDatabase(dbConfig, dataSource);
                if (restoreSucceeded) {
                    return ResponseEntity.ok("Successfully restored database: " + dbName);

                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to restore database: " + dbName);

                }


            }

        }


        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Database configuration not found for: " + dbName + " (" + dbType + ")");
    }

}





