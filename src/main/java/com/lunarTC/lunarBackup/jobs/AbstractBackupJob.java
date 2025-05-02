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


        GlobalConfig globalConfig = globalConfigLoader.loadGlobalConfig();

        List<DatabaseConfig> databaseConfigs = globalConfig.getDatabaseConfigList();

        List<String> summaryEmailList = globalConfig.getNotificationConfig().getNotificationSummaryEmailToList();


        List<DatabaseConfig> failedDatabases = new ArrayList<>();


        for (DatabaseConfig config : databaseConfigs) {

            if (shouldRunBackup(config)) {

//                System.out.println("Running " + config.getDatabaseName() + "   " + getBackupType());
                boolean backupSucceeded = backupService.backupDatabase(config, getBackupType());
                if (!backupSucceeded) {
                    if (!failedDatabases.contains(config)) {
                        failedDatabases.add(config);
//                        System.out.println("Failed from job " + config.getDatabaseName() + "   " + getBackupType());

                    }
                } else if (backupSucceeded) {
//                    System.out.println("Successful from job " + config.getDatabaseName() + "   " + getBackupType());


                }


            }

        }


        if (summaryEmailList != null && !summaryEmailList.isEmpty()) {
            for (String emailTo : summaryEmailList) {
                mailService.sendBackupSummaryEmail(emailTo, failedDatabases, databaseConfigs.size());
            }
        }


        if (!failedDatabases.isEmpty()) {

            try {
                System.out.println("There are " + failedDatabases.size() + " databases,retry them after 1 hour ");
                Thread.sleep(1000); //1 hour
            } catch (InterruptedException e) {
                throw new RuntimeException("Retry sleep interrupted", e);
            }
            int tries = 0;
            int initialFailedCount = failedDatabases.size();


            while (!failedDatabases.isEmpty() && tries < 3) {

                System.out.println("Retry number " + (tries + 1) + " for failed databases");

                Iterator<DatabaseConfig> iterator = failedDatabases.iterator();
                while (iterator.hasNext()) {
                    DatabaseConfig config = iterator.next();
                    if (shouldRunBackup(config)) {


                        System.out.println("Retry: " + getBackupType() + "   :" + config.getDatabaseName() + " ======> " + config.getType());
                        boolean backupSucceeded = backupService.backupDatabase(config, getBackupType());
                        if (backupSucceeded) {
                            iterator.remove();

                        }


                    }


                }
                tries++;
                try {
                    Thread.sleep(1000); //1 hour
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }


            }
            if (summaryEmailList != null && !summaryEmailList.isEmpty()) {
                for (String emailTo : summaryEmailList) {
                    mailService.sendRetrySummaryEmail(emailTo, failedDatabases, initialFailedCount, tries);
                }
            }


        }


    }


    protected abstract boolean shouldRunBackup(DatabaseConfig config);

    protected abstract String getBackupType();  // new method

}
