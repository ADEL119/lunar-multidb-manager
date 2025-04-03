package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.models.DatabaseConfig;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class DailyBackupJob extends AbstractBackupJob {
    @Override
    protected boolean shouldRunBackup(DatabaseConfig config) {
        return config.getDaily();
    }
    @Override
    protected String getFrequency() {
        return "daily";
    }

}
