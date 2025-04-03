package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.configs.DatabaseConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.services.BackupService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

public abstract class AbstractBackupJob implements Job {

    @Autowired
    private BackupService backupService;
    @Autowired
    private DatabaseConfigLoader configLoader;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {


        List<DatabaseConfig> databaseConfigs = configLoader.loadDatabaseConfigs();

        for (DatabaseConfig config : databaseConfigs) {
            if (shouldRunBackup(config)) {
                System.out.println("Running backup for: " + config.getDatabaseName());
                backupService.backupDatabase(config,getFrequency());
            }
        }
    }

    protected abstract boolean shouldRunBackup(DatabaseConfig config);
    protected abstract String getFrequency();  // new method

}
