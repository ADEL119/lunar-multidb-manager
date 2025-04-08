package com.lunarTC.lunarBackup.models;


import lombok.Data;

@Data
public class DynamicCronRequest {

    private String cronExpression;
    private String frequencyLabel;
}
