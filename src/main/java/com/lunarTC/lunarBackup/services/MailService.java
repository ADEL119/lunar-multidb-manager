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
        <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
            <div style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
                <h2 style="color: #4CAF50;"> Backup Successful</h2>
                <p><strong>Database:</strong> %s</p>
                <p><strong>Type:</strong> %s</p>
                <p><strong>Frequency:</strong> %s</p>
                <p><strong>Backup File:</strong> %s</p>
                <hr>
                <p style="font-size: 0.9em; color: #888;">This is an automated message from your backup system.</p>
            </div>
        </body>
        </html>
    """.formatted(dbName, dbType, frequency, filePath);
    }

    public String buildBackupFailureEmail(String dbName, String dbType, String frequency, String errorMessage) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
            <div style="border: 1px solid #f44336; border-radius: 8px; padding: 20px; background-color: #fff5f5;">
                <h2 style="color: #f44336;"> Backup Failed</h2>
                <p><strong>Database:</strong> %s</p>
                <p><strong>Type:</strong> %s</p>
                <p><strong>Frequency:</strong> %s</p>
                <p><strong>Error Message:</strong></p>
                <pre style="background-color: #fce4ec; padding: 10px; border-radius: 4px; color: #c62828;">%s</pre>
                <hr>
                <p style="font-size: 0.9em; color: #888;">Please check the system or database logs for more details.<br>This is an automated alert from your backup system.</p>
            </div>
        </body>
        </html>
    """.formatted(dbName, dbType, frequency, errorMessage != null ? errorMessage : "Unknown error");
    }
    public void sendBackupSummaryEmail(String to, List<DatabaseConfig> failedDatabases, int totalDatabases) {
        try {
            int successCount = totalDatabases - failedDatabases.size();
            double successRate = (totalDatabases == 0) ? 0 : (successCount * 100.0 / totalDatabases);

            String subject = String.format("%.0f%% of Backups Succeeded", successRate);

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
            <div style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
                <h2>Backup Summary</h2>
                <p><strong>Total Databases:</strong> %d</p>
                <p><strong>Successful:</strong> %d</p>
                <p><strong>Failed:</strong> %d</p>
            """.formatted(totalDatabases, successCount, failedDatabases.size()));

            if (!failedDatabases.isEmpty()) {
                htmlContent.append("<h3 style='color: red;'>Failed Databases:</h3><ul>");
                for (DatabaseConfig config : failedDatabases) {
                    htmlContent.append("<li>").append(config.getDatabaseName()).append(" (").append(config.getType()).append(")</li>");
                }
                htmlContent.append("</ul>");
            } else {
                htmlContent.append("<p style='color: green;'>All databases backed up successfully ✅</p>");
            }

            htmlContent.append("""
                <hr>
                <p style="font-size: 0.9em; color: #888;">This is an automated backup summary notification.</p>
            </div>
            </body>
            </html>
            """);

            sendHtmlEmail(to, subject, htmlContent.toString());

        } catch (Exception e) {
            System.err.println("Failed to send backup summary email: " + e.getMessage());
        }
    }


}
