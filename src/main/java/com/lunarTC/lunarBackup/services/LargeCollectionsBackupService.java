package com.lunarTC.lunarBackup.services;

import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.utils.DatabaseUtils;

import java.io.File;

public class LargeCollectionsBackupService {


    public boolean backupLargeCollections(DatabaseConfig config,String backupType){

        String backupDirectoryPath= DatabaseUtils.getBackupDirectoryPath(config,backupType);
        File backupDir = new File(backupDirectoryPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }



    }

}
