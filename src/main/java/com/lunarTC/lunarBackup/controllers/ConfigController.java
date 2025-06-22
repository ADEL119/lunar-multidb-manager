package com.lunarTC.lunarBackup.controllers;


import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    GlobalConfigLoader globalConfigLoader;


    @GetMapping("/getAll")
    public ResponseEntity<List<DatabaseConfig>> getAllDatabaseConfig(){

        List<DatabaseConfig> databaseConfigs=globalConfigLoader.loadGlobalConfig().getDatabaseConfigList();


        return ResponseEntity.ok(databaseConfigs);
    }
}
