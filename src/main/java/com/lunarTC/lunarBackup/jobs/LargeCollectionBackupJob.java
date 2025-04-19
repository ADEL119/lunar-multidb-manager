package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.models.DatabaseConfig;

public class LargeCollectionBackupJob extends AbstractBackupJob{

    @Override
    protected boolean shouldRunBackup(DatabaseConfig config) {
        return config.getBackupLargeCollections();
    }

    @Override
    protected String getBackupType() {
        return "daily";
    }



}
