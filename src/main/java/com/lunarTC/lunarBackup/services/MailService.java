package com.lunarTC.lunarBackup.services;

import com.lunarTC.lunarBackup.models.DatabaseConfig;
import com.lunarTC.lunarBackup.models.EmailConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.List;
import java.util.Properties;

@Service
public class MailService {

    private final EmailConfig emailConfig;

    public MailService(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    private JavaMailSenderImpl createMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfig.getNotificationSmtpHost());
        mailSender.setPort(emailConfig.getNotificationSmtpPort());
        mailSender.setUsername(emailConfig.getNotificationEmailFrom());
        mailSender.setPassword(emailConfig.getNotificationEmailPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", emailConfig.getNotificationSmtpAuth());
        props.put("mail.smtp.starttls.enable", emailConfig.getNotificationStartTlsEnable());
        return mailSender;
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            JavaMailSenderImpl mailSender = createMailSender();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getNotificationEmailFrom(), emailConfig.getNotificationSenderViewName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error while sending email: " + e.getMessage());
        }
    }

    public String buildBackupSuccessEmail(String dbName, String dbType, String frequency, String filePath) {
        return """
        <html>
        <body style="font-family: 'Segoe UI', sans-serif; background-color: #f9f9f9; margin: 0; padding: 30px;">
            <div style="width: 100%%; background: white; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); overflow: hidden; box-sizing: border-box;">
                <div style="background: linear-gradient(to right, #4CAF50, #81C784); padding: 20px; color: white;">
                    <h2 style="margin: 0;">✅ Backup Successful</h2>
                </div>
                <div style="padding: 20px; font-size: 16px;">
                    <p><strong>Database:</strong> %s</p>
                    <p><strong>Type:</strong> %s</p>
                    <p><strong>Frequency:</strong> %s</p>
                    <p><strong>Backup File:</strong> %s</p>
                </div>
                <div style="padding: 15px; background-color: #f1f1f1; text-align: center; font-size: 12px; color: #777;">
                    This is an automated message from your backup system.
                </div>
            </div>
        </body>
        </html>
        """.formatted(dbName, dbType, frequency, filePath);
    }

    public String buildBackupFailureEmail(String dbName, String dbType, String frequency, String errorMessage) {
        return """
        <html>
        <body style="font-family: 'Segoe UI', sans-serif; background-color: #fff5f5; margin: 0; padding: 30px;">
            <div style="width: 100%%; background: white; border-radius: 10px; box-shadow: 0 2px 10px rgba(255,0,0,0.1); overflow: hidden; box-sizing: border-box;">
                <div style="background: linear-gradient(to right, #f44336, #e57373); padding: 20px; color: white;">
                    <h2 style="margin: 0;">❌ Backup Failed</h2>
                </div>
                <div style="padding: 20px; font-size: 16px;">
                    <p><strong>Database:</strong> %s</p>
                    <p><strong>Type:</strong> %s</p>
                    <p><strong>Frequency:</strong> %s</p>
                    <p><strong>Error Message:</strong></p>
                    <pre style="background: #fce4ec; padding: 15px; border-radius: 5px; color: #c62828;">%s</pre>
                </div>
                <div style="padding: 15px; background-color: #fce4ec; text-align: center; font-size: 12px; color: #888;">
                    Please check the logs for more info. This is an automated alert.
                </div>
            </div>
        </body>
        </html>
        """.formatted(dbName, dbType, frequency, errorMessage != null ? errorMessage : "Unknown error");
    }

    public void sendBackupSummaryEmail(String to, List<DatabaseConfig> failedDatabases, int totalDatabases, String backupType) {
        try {
            int successCount = totalDatabases - failedDatabases.size();
            double successRate = (totalDatabases == 0) ? 0 : (successCount * 100.0 / totalDatabases);

            String subject = String.format("📊 %.0f%% of Backups Succeeded : " + backupType, successRate);

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("""
            <html>
            <body style="font-family: 'Segoe UI', sans-serif; background-color: #f9f9f9; margin: 0; padding: 30px;">
            <div style="width: 100%%; background: white; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); box-sizing: border-box;">
                <div style="background: linear-gradient(to right, #2196F3, #64B5F6); padding: 20px; color: white;">
                    <h2 style="margin: 0;">📊 Backup Summary</h2>
                </div>
                <div style="padding: 20px;">
                    <p style="font-size: 16px;"><strong>Total Databases:</strong> %d</p>
                    <p style="font-size: 16px;"><strong>Successful:</strong> %d</p>
                    <p style="font-size: 16px;"><strong>Failed:</strong> %d</p>
            """.formatted(totalDatabases, successCount, failedDatabases.size()));

            if (!failedDatabases.isEmpty()) {
                htmlContent.append("<h3 style='color: #e53935; font-size: 16px;'>❌ Failed Databases:</h3><ul style='padding-left:20px;'>");
                for (DatabaseConfig config : failedDatabases) {
                    htmlContent.append("<li style='font-size: 16px; font-weight: bold;'>")
                            .append(config.getDatabase()).append(" (").append(config.getType()).append(")</li>");
                }
                htmlContent.append("</ul>");
            } else {
                htmlContent.append("<p style='color: #43a047; font-weight: bold;'>✅ All backups succeeded</p>");
            }

            htmlContent.append("""
                </div>
                <div style="padding: 15px; background-color: #f1f1f1; text-align: center; font-size: 12px; color: #777;">
                    This is an automated backup summary notification.
                </div>
            </div>
            </body>
            </html>
            """);

            sendHtmlEmail(to, subject, htmlContent.toString());

        } catch (Exception e) {
            System.err.println("Failed to send backup summary email: " + e.getMessage());
        }
    }

    public void sendRetrySummaryEmail(String to, List<DatabaseConfig> stillFailedDatabases, int initialFailedCount, int retryAttempts, String backupType) {
        try {
            int recoveredCount = initialFailedCount - stillFailedDatabases.size();
            double recoveryRate = (initialFailedCount == 0) ? 0 : (recoveredCount * 100.0 / initialFailedCount);

            String subject = String.format("🛠️ Retry Summary – %.0f%% Recovered : " + backupType, recoveryRate);

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("""
            <html>
            <body style="font-family: 'Segoe UI', sans-serif; background-color: #f9f9f9; margin: 0; padding: 30px;">
            <div style="width: 100%%; background: white; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); box-sizing: border-box;">
                <div style="background: linear-gradient(to right, #FFA000, #FFCA28); padding: 20px; color: white;">
                    <h2 style="margin: 0;">🔄 Retry Summary</h2>
                </div>
                <div style="padding: 20px;">
                    <p style="font-size: 16px;"><strong>Initial Failed Databases:</strong> %d</p>
                    <p style="font-size: 16px;"><strong>Recovered after Retry:</strong> %d</p>
                    <p style="font-size: 16px;"><strong>Still Failing:</strong> %d</p>
                    <p style="font-size: 16px;"><strong>Total Retry Attempts:</strong> %d</p>
            """.formatted(initialFailedCount, recoveredCount, stillFailedDatabases.size(), retryAttempts));

            if (!stillFailedDatabases.isEmpty()) {
                htmlContent.append("<h3 style='color: #d32f2f; font-size: 16px;'>❌ Still Failing:</h3><ul style='padding-left:20px;'>");
                for (DatabaseConfig config : stillFailedDatabases) {
                    htmlContent.append("<li style='font-size: 16px; font-weight: bold;'>")
                            .append(config.getDatabase())
                            .append(" (")
                            .append(config.getType())
                            .append(")</li>");
                }
                htmlContent.append("</ul>");
            } else {
                htmlContent.append("<p style='color: #43a047; font-weight: bold;'>✅ All failures recovered</p>");
            }

            htmlContent.append("""
                </div>
                <div style="padding: 15px; background-color: #f1f1f1; text-align: center; font-size: 12px; color: #777;">
                    This is an automated retry summary notification.
                </div>
            </div>
            </body>
            </html>
            """);

            sendHtmlEmail(to, subject, htmlContent.toString());

        } catch (Exception e) {
            System.err.println("Failed to send retry summary email: " + e.getMessage());
        }
    }
}
