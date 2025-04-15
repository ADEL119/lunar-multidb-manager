package com.lunarTC.lunarBackup.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("adelselmi8@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = enable HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email: " + e.getMessage());
        }
    }
    public  String buildBackupSuccessEmail(String dbName, String dbType, String frequency, String filePath) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
            <div style="border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
                <h2 style="color: #4CAF50;">✅ Backup Successful</h2>
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
                <h2 style="color: #f44336;">❌ Backup Failed</h2>
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


}
