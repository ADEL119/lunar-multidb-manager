package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.configs.DatabaseConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.services.BackupService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractBackupJob implements Job {

    @Autowired
    private BackupService backupService;
    @Autowired
    private DatabaseConfigLoader configLoader;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {


        List<DatabaseConfig> databaseConfigs = configLoader.loadDatabaseConfigs();
        List<DatabaseConfig> failedDatabases=new ArrayList<>();



        for (DatabaseConfig config : databaseConfigs) {
            if (shouldRunBackup(config)) {
                System.out.println("Running backup for: " + config.getDatabaseName());
                boolean backupSucceeded= backupService.backupDatabase(config,getFrequency());
                if(!backupSucceeded)
                {
                    if(!failedDatabases.contains(config)) {
                        failedDatabases.add(config);
                    }                }
            }
        }
        if(!failedDatabases.isEmpty()){
            try {
                System.out.println("There are "+failedDatabases.size()+" databases,retry them after 10 seconds");
                Thread.sleep(10000); //10 seconds
            } catch (InterruptedException e) {
                throw new RuntimeException("Retry sleep interrupted",e);
            }
            int tries=0;
            while(tries<10 && !failedDatabases.isEmpty()){

                Iterator<DatabaseConfig> iterator = failedDatabases.iterator();
                while (iterator.hasNext()) {
                    DatabaseConfig config = iterator.next();
                    if (shouldRunBackup(config)) {
                        System.out.println("Retry backup for: " + config.getDatabaseName());
                        boolean backupSucceeded = backupService.backupDatabase(config, getFrequency());
                        if (backupSucceeded) {
                            iterator.remove();
                        }
                    }
                }

                tries++;

            }


        }


    }

    protected abstract boolean shouldRunBackup(DatabaseConfig config);
    protected abstract String getFrequency();  // new method

}
