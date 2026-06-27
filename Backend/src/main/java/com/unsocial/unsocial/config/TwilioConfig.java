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
    @Value("${notification.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @PostConstruct
    public void init() {
        if ((smsEnabled || whatsappEnabled) && !accountSid.isBlank() && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            log.info("✅ Twilio initialized successfully (SMS: {}, WhatsApp: {})", smsEnabled, whatsappEnabled);
        } else {
            log.info("ℹ️  Twilio disabled — set notification.sms.enabled=true and/or notification.whatsapp.enabled=true in properties to enable");
        }
    }
}

