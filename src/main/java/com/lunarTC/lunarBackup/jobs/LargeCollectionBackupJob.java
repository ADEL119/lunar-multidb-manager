package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.configs.GlobalConfigLoader;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import com.lunarTC.lunarBackup.services.MailService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import com.lunarTC.lunarBackup.services.LargeCollectionsBackupService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LargeCollectionBackupJob implements Job {

    @Autowired
    private GlobalConfigLoader globalConfigLoader;

    @Autowired
    private LargeCollectionsBackupService largeCollectionsBackupService;

    @Autowired
    private MailService mailService;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        GlobalConfig globalConfig = globalConfigLoader.loadGlobalConfig();

        List<DatabaseConfig> databaseConfigs = globalConfigLoader.loadGlobalConfig().getDatabaseConfigList();
        List<DatabaseConfig> failedDatabases = new ArrayList<>();
        List<String> summaryEmailList = globalConfig.getNotificationConfig().getNotificationSummaryEmailToList();


        for (DatabaseConfig config : databaseConfigs) {
            if (shouldRunLargeCollections(config)) {
                System.out.println("Running largeCollections backup for: " + config.getDatabase()+" "+ LocalDateTime.now());
                boolean backupSucceeded = largeCollectionsBackupService.backupLargeCollections(config, "Large_Collections");
                if (!backupSucceeded) {
                    if (!failedDatabases.contains(config)) {
                        failedDatabases.add(config);
                    }

                }


            }
        }
        if (summaryEmailList != null && !summaryEmailList.isEmpty()) {
            for (String emailTo : summaryEmailList) {
                mailService.sendBackupSummaryEmail(emailTo, failedDatabases, databaseConfigs.size(),"Large_Collections");
            }
        }

        if (!failedDatabases.isEmpty()) {
            try {
                System.out.println("There are " + failedDatabases.size() + " failed database(s) with large collections,retry them after 1 hour");
                Thread.sleep(3600000); //1 hour
            } catch (InterruptedException e) {
                throw new RuntimeException("Retry sleep interrupted", e);
            }
            int tries = 0;
            int initialFailedCount = failedDatabases.size();


            while (!failedDatabases.isEmpty() && tries < 10) {

                System.out.println("Retry number " + (tries + 1) + " for failed databases"+" "+ LocalDateTime.now());

                Iterator<DatabaseConfig> iterator = failedDatabases.iterator();
                while (iterator.hasNext()) {
                    DatabaseConfig config = iterator.next();
                    if (shouldRunLargeCollections(config)) {


                        System.out.println("Retry backup for: " + config.getDatabase()+" "+ LocalDateTime.now());
                        boolean backupSucceeded = largeCollectionsBackupService.backupLargeCollections(config, "Large_Collections");
                        if (backupSucceeded) {
                            iterator.remove();

                        }

                    }

                }

                tries++;
                try {
                    Thread.sleep(1800000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (summaryEmailList != null && !summaryEmailList.isEmpty()) {
                for (String emailTo : summaryEmailList) {
                    mailService.sendRetrySummaryEmail(emailTo, failedDatabases, initialFailedCount, tries,"Large_Collections");
                }
            }


        }


    }


    public Boolean shouldRunLargeCollections(DatabaseConfig config) {

        String type = config.getType().toLowerCase();

        if (type.equals("mongo") && config.getBackupLargeCollections() && !config.getLargeCollections().isEmpty()) {

            return true;
        }
        return false;
    }

}
