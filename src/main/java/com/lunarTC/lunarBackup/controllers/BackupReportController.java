package com.lunarTC.lunarBackup.controllers;

import com.lunarTC.lunarBackup.models.BackupReport;
import com.lunarTC.lunarBackup.services.BackupReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class BackupReportController {

    @Autowired
    private BackupReportService backupReportService;

    @GetMapping
    public List<BackupReport> getAllReports() {
        return backupReportService.getAllReports();
    }
}
