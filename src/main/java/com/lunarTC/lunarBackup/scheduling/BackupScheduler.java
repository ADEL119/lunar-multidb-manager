package com.lunarTC.lunarBackup.scheduling;

import com.lunarTC.lunarBackup.jobs.DailyBackupJob;
import com.lunarTC.lunarBackup.jobs.WeeklyBackupJob;
import com.lunarTC.lunarBackup.jobs.MonthlyBackupJob;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackupScheduler {

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void scheduleBackups() throws SchedulerException {
        scheduleBackup(DailyBackupJob.class, "daily", "0 0/1 * * * ?");    // Every 1 minute
        scheduleBackup(WeeklyBackupJob.class, "weekly", "0 0/2 * * * ?");  // Every 2 minutes
        scheduleBackup(MonthlyBackupJob.class, "monthly", "0 0/3 * * * ?"); // Every 3 minutes
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
}
