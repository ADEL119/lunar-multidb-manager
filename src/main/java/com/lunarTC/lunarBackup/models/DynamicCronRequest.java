package com.lunarTC.lunarBackup.models;


import lombok.Data;
import org.quartz.CronScheduleBuilder;

@Data
public class DynamicCronRequest {

    private String cronExpression;
    private String frequencyLabel;

    public boolean isValidCron() {
        try {
            CronScheduleBuilder.cronSchedule(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
