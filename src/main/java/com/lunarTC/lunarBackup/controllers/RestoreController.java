package com.lunarTC.lunarBackup.controllers;


import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import com.lunarTC.lunarBackup.services.BackupService;
import com.lunarTC.lunarBackup.services.RestoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restore")
public class RestoreController {

    @Autowired
    GlobalConfigLoader globalConfigLoader;

    @Autowired
    RestoreService restoreService;

    @GetMapping
    public String restoreDatabase(@PathVariable String dbName,@PathVariable String dbType,@PathVariable String dataSource){

        GlobalConfig globalConfig=globalConfigLoader.loadGlobalConfig();

        List<DatabaseConfig> databaseConfigs = globalConfig.getDatabaseConfigList();

        for(DatabaseConfig dbConfig :databaseConfigs){

            if(dbConfig.getDatabaseName().equalsIgnoreCase(dbName) && dbConfig.getType().equalsIgnoreCase(dbType))
            {

                boolean restoreSucceeded=restoreService.restoreDatabase(dbConfig,dataSource);
                if(restoreSucceeded)
                {
                    System.out.println("Successful restore database :"+dbName);

                }
                else
                {
                    System.out.println("failed to restore database :"+dbName);

                }




            }

        }





    }




}
