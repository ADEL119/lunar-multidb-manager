package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import com.lunarTC.lunarBackup.services.BackupService;
import com.lunarTC.lunarBackup.services.MailService;
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

    @Autowired
    private MailService mailService;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {


        GlobalConfig globalConfig=globalConfigLoader.loadGlobalConfig();

        List<DatabaseConfig> databaseConfigs = globalConfig.getDatabaseConfigList();

        List<DatabaseConfig> failedDatabases=new ArrayList<>();



        for (DatabaseConfig config : databaseConfigs) {

            if (shouldRunBackup(config)) {

                System.out.println( "Running "+ config.getDatabaseName()+"   "+getBackupType());
                boolean backupSucceeded= backupService.backupDatabase(config, getBackupType());
                if(!backupSucceeded)
                {
                    if(!failedDatabases.contains(config)) {
                        failedDatabases.add(config);
                        System.out.println( "Failed from job "+ config.getDatabaseName()+"   "+getBackupType());

                    }
                } else if (backupSucceeded) {
                    System.out.println( "Successful from job "+ config.getDatabaseName()+"   "+getBackupType());


                }


            }

        }

        List<String> summaryEmailList = globalConfig.getNotificationConfig().getNotificationSummaryEmailToList();

        if (summaryEmailList != null && !summaryEmailList.isEmpty()) {
            for (String emailTo : summaryEmailList) {
                mailService.sendBackupSummaryEmail(emailTo, failedDatabases, databaseConfigs.size());
            }
        }



        if( ! failedDatabases.isEmpty()){
            try {
                System.out.println("There are "+failedDatabases.size()+" databases,retry them after 1 hour");
                Thread.sleep(3600000); //1 hour
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
                        if (tries>=3)
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
