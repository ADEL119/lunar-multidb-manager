package com.lunarTC.lunarBackup.scheduling;

import com.lunarTC.lunarBackup.jobs.DailyBackupJob;
import com.lunarTC.lunarBackup.jobs.DynamicBackupJob;
import com.lunarTC.lunarBackup.jobs.WeeklyBackupJob;
import com.lunarTC.lunarBackup.jobs.MonthlyBackupJob;
import com.lunarTC.lunarBackup.models.DatabaseConfig;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class BackupScheduler {

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void schedulePredefinedBackups() throws SchedulerException {
        scheduleBackup(DailyBackupJob.class, "daily", "0 0/1 * * * ?");    // Every 2 minute
        scheduleBackup(WeeklyBackupJob.class, "weekly", "0 0/3 * * * ?");  // Every 10 minutes
        scheduleBackup(MonthlyBackupJob.class, "monthly", "0 0/5 * * * ?"); // Every 30 minutes
    }

    private void scheduleBackup(Class<? extends Job> jobClass, String frequency, String cronExpression) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(frequency + "_backup")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(frequency + "_trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }
    public void scheduleDynamicBackupJob(Scheduler scheduler, DatabaseConfig config, String cronExpression, String frequency) throws SchedulerException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("databaseConfig", config);
        dataMap.put("frequency", frequency);

        String jobId = UUID.randomUUID().toString();

        JobDetail jobDetail = JobBuilder.newJob(DynamicBackupJob.class)
                .withIdentity("dynamicJob_" + jobId, "dynamicGroup")
                .usingJobData(dataMap)
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + jobId, "dynamicGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }


}
