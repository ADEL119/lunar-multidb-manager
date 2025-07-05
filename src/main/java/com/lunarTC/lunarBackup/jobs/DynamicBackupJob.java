package com.lunarTC.lunarBackup.jobs;

import com.lunarTC.lunarBackup.models.DatabaseConfig;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

@DisallowConcurrentExecution
public class DynamicBackupJob extends AbstractBackupJob {

    private String frequencyLabel = "Dynamic";

    @Override
    protected void beforeBackupExecution(JobExecutionContext context) {
        Object freq = context.getJobDetail().getJobDataMap().get("frequency");
        if (freq != null) {
            frequencyLabel = freq.toString();
        }
    }


    @Override
    protected boolean shouldRunBackup(DatabaseConfig config) {
        return true;
    }

    @Override
    protected String getBackupType() {
        return frequencyLabel;
    }
}
