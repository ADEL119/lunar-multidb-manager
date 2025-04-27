package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.models.DatabaseConfig;
import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public class DynamicBackupJob extends AbstractBackupJob {


    @Override
    protected boolean shouldRunBackup(DatabaseConfig config) {
        return true;
    }

    @Override
    protected String getBackupType() {
        return "Dynamic";
    }
}
