package com.lunarTC.lunarBackup.models;


import lombok.Data;

import java.util.List;

@Data
public class GlobalConfig {
    

    private EmailConfig notificationConfig;
    private List<DatabaseConfig> databaseConfigList;

}


