package com.lunarTC.lunarBackup.configs;

import com.lunarTC.lunarBackup.models.EmailConfig;
import com.lunarTC.lunarBackup.models.GlobalConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class EmailConfigProvider {

    @Autowired
    private GlobalConfigLoader globalConfigLoader;

    @Bean
    public EmailConfig emailConfig() {

        return globalConfigLoader.loadGlobalConfig().getNotificationConfig();
    }
}
