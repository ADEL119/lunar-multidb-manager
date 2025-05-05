package com.lunarTC.lunarBackup.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields in JSON
public class DatabaseConfig implements Serializable {
    private static final long serialVersionUID = 1L; // Helps with versioning

    private String type="mongo";

    private String host;

    private int port;

    private String username;

    private String password;

    private String authenticationDatabase;

    private String database;

    private String shortName;

    private List<String> emailList=new ArrayList<>();


    private Boolean daily = true;
    private Boolean weekly = true;
    private Boolean monthly = true;

    private List<String> largeCollections=new ArrayList<>();

    private Boolean backupLargeCollections=true;

}
