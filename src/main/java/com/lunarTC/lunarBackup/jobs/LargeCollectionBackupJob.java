package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import com.lunarTC.lunarBackup.services.LargeCollectionsBackupService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LargeCollectionBackupJob implements Job{

    @Autowired
    private GlobalConfigLoader globalConfigLoader;

    @Autowired
    private LargeCollectionsBackupService largeCollectionsBackupService;



    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        List<DatabaseConfig> databaseConfigs = globalConfigLoader.loadGlobalConfig().getDatabaseConfigList();
        List<DatabaseConfig> failedDatabases = new ArrayList<>();

        for(DatabaseConfig config : databaseConfigs)
        {
            if( shouldRunLargeCollections(config))
            {
                System.out.println("Running largeCollections backup for: "+config.getDatabaseName());
                boolean backupSucceeded = largeCollectionsBackupService.backupLargeCollections(config, "Large_Collections");
                if(!backupSucceeded )
                {
                    if(!failedDatabases.contains(config)){
                        failedDatabases.add(config);
                    }

                }


            }
        }
        if (!failedDatabases.isEmpty()) {
            try {
                System.out.println("There are " + failedDatabases.size() + " failed large collections,retry them after 1 hour");
                Thread.sleep(3000); //1 hour
            } catch (InterruptedException e) {
                throw new RuntimeException("Retry sleep interrupted", e);
            }

            while (!failedDatabases.isEmpty()) {

                Iterator<DatabaseConfig> iterator = failedDatabases.iterator();
                while (iterator.hasNext()) {
                    DatabaseConfig config = iterator.next();
                    if( shouldRunLargeCollections(config)) {
                        int tries = 0;
                        while (tries < 10) {
                            System.out.println("Retry backup for: " + config.getDatabaseName());
                            boolean backupSucceeded = largeCollectionsBackupService.backupLargeCollections(config, "Large_Collections");
                            if (backupSucceeded) {
                                iterator.remove();
                                break;
                            }
                            tries++;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                        }
                        if (tries >= 10) {
                            System.out.println("Max Retry reached for database:" + config.getDatabaseName());
                            //should send mail here
                            iterator.remove();


                        }
                    }
                }


            }


        }











    }

    public Boolean shouldRunLargeCollections(DatabaseConfig config){

        String type=config.getType().toLowerCase();

        if(type.equals("mongo") && config.getBackupLargeCollections() && ! config.getLargeCollections().isEmpty()){

            return true;
        }
        return false;
    }

}
