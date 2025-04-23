package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
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
    private GlobalConfigLoader globalConfigLoader;



    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {


        List<DatabaseConfig> databaseConfigs = globalConfigLoader.loadGlobalConfig().getDatabaseConfigList();
        List<DatabaseConfig> failedDatabases=new ArrayList<>();



        for (DatabaseConfig config : databaseConfigs) {

            if (shouldRunBackup(config)) {

                System.out.println("Running backup for: " + config.getDatabaseName());
                boolean backupSucceeded= backupService.backupDatabase(config, getBackupType());
                if(!backupSucceeded)
                {
                    if(!failedDatabases.contains(config)) {
                        failedDatabases.add(config);
                    }                }
            }

        }

        if( ! failedDatabases.isEmpty()){
            try {
                System.out.println("There are "+failedDatabases.size()+" databases,retry them after 1 hour");
                Thread.sleep(20000); //1 hour
            } catch (InterruptedException e) {
                throw new RuntimeException("Retry sleep interrupted",e);
            }

            while( !failedDatabases.isEmpty()){

                Iterator<DatabaseConfig> iterator = failedDatabases.iterator();
                while (iterator.hasNext()) {
                    DatabaseConfig config = iterator.next();
                    if (shouldRunBackup(config)) {
                        int tries=0;
                        while(tries<3 ) {
                            System.out.println("Retry backup for: " + config.getDatabaseName());
                            boolean backupSucceeded = backupService.backupDatabase(config, getBackupType());
                            if (backupSucceeded) {
                                iterator.remove();
                                break;
                            }
                            tries++;
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                        }
                        if (tries>=10)
                        {
                          System.out.println("Max Retry reached for database:"+config.getDatabaseName());
                          //should send mail here
                          iterator.remove();


                        }
                    }
                }


            }


        }


    }

    protected abstract boolean shouldRunBackup(DatabaseConfig config);
    protected abstract String getBackupType();  // new method

}
