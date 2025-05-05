package com.lunarTC.lunarBackup.models;


import lombok.Data;

import java.util.List;

@Data
public class GlobalConfig {
    
    private String pathDirectory;
    private EmailConfig notificationConfig;
    private List<DatabaseConfig> databaseConfigList;

}


