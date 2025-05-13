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

    private String database;
    private String authenticationDatabase;
    private List<String> emailList = new ArrayList<>();
    private String password;
    private Boolean backupLargeCollections = true;
    private Boolean monthly = true;
    private String host;
    private String shortName;
    private List<String> largeCollections = new ArrayList<>();
    private String username;
    private Boolean daily = true;
    private int port;
    private Boolean weekly = true;
    private String type = "mongo";
}
