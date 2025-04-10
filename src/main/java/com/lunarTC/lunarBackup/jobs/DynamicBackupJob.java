package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.models.DatabaseConfig;

public class DynamicBackupJob extends AbstractBackupJob {


    @Override
    protected boolean shouldRunBackup(DatabaseConfig config) {
        return true;
    }

    @Override
    protected String getFrequency() {
        return "Dynamic";
    }
}
