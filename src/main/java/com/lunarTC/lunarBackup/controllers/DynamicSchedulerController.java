package com.lunarTC.lunarBackup.controllers;

import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.DynamicCronRequest;
import com.lunarTC.lunarBackup.scheduling.BackupScheduler;
import com.lunarTC.lunarBackup.services.BackupService;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
public class DynamicSchedulerController {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private BackupService backupService;
    @Autowired
    private BackupScheduler backupScheduler;

    @Autowired
    private GlobalConfigLoader globalConfigLoader;

    @PostMapping("/backup-now")
    public ResponseEntity<String> triggerImmediateBackupForAll() {
        try {
            List<DatabaseConfig> configs = globalConfigLoader.loadGlobalConfig().getDatabaseConfigList();
            for (DatabaseConfig config : configs) {
                backupService.backupDatabase(config, "Manual");
            }
            return ResponseEntity.ok("Urgent backup executed for all databases.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to perform urgent backup: " + e.getMessage());
        }
    }

    @PostMapping("/schedule-dynamic")
    public ResponseEntity<String> scheduleDynamicCronBackup(@RequestBody DynamicCronRequest request) {
        try {
            List<DatabaseConfig> configs = globalConfigLoader.loadGlobalConfig().getDatabaseConfigList();
            for (DatabaseConfig config : configs) {

                backupScheduler.scheduleDynamicBackupJob(scheduler, config, request.getCronExpression(), request.getFrequencyLabel());
            }
            return ResponseEntity.ok("Dynamic backup jobs scheduled with cron: " + request.getCronExpression());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to schedule dynamic backup: " + e.getMessage());
        }
    }

}
