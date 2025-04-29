package com.lunarTC.lunarBackup.models;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class EmailConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> notificationSummaryEmailToList = new ArrayList<>();
    private String notificationEmailFrom;
    private String notificationSenderViewName;
    private String notificationEmailPassword;
    private String notificationSmtpHost;
    private int notificationSmtpPort;
    private Boolean notificationSmtpAuth;
    private Boolean notificationStartTlsEnable;
}
