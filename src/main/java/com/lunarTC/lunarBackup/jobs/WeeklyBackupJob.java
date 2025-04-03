package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.models.DatabaseConfig;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class WeeklyBackupJob extends AbstractBackupJob {
    @Override
    protected boolean shouldRunBackup(DatabaseConfig config) {
        return config.getWeekly();
    }
    @Override
    protected String getFrequency() {
        return "weekly";
    }
}
