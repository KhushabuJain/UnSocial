package com.unsocial.unsocial.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TwilioConfig {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @PostConstruct
    public void init() {
        if (smsEnabled && !accountSid.isBlank() && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            log.info("✅ Twilio SMS initialized successfully");
        } else {
            log.info("ℹ️  Twilio SMS disabled — set notification.sms.enabled=true in properties to enable");
        }
    }
}
