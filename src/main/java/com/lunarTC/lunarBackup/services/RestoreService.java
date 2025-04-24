package com.lunarTC.lunarBackup.services;


import com.lunarTC.lunarBackup.models.DatabaseConfig;
import org.springframework.stereotype.Service;

@Service
public class RestoreService {


    public Boolean restoreDatabase(DatabaseConfig config,String backupSource)
    {

        String type=config.getType().toLowerCase();
        switch(type) {
            case "mysql":
            case "mariadb":



            case "mongo":




            case "postgres":



        }


    return false;}



}
