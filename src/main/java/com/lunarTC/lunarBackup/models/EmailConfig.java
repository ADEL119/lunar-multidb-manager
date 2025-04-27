package com.lunarTC.lunarBackup.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EmailConfig {

    private List<String> notificationSummaryEmailToList = new ArrayList<>();
    private String notificationEmailFrom;
    private String notificationSenderViewName;
    private String notificationEmailPassword;
    private String notificationSmtpHost;
    private int notificationSmtpPort;
    private Boolean notificationSmtpAuth;
    private Boolean notificationStartTlsEnable;


}
