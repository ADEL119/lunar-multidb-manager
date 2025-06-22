package com.lunarTC.lunarBackup.controllers;

import com.lunarTC.lunarBackup.models.BackupReport;
import com.lunarTC.lunarBackup.services.BackupReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class BackupReportController {

    @Autowired
    private BackupReportService backupReportService;

    @GetMapping
    public ResponseEntity<List<BackupReport>> getAllReports() {
        List<BackupReport> reports=backupReportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/success")
    public ResponseEntity<List<BackupReport>> getSuccessfulReports(){
        List<BackupReport> successfulReports=backupReportService.getAllReports();
        return ResponseEntity.ok(successfulReports);
    }
}
