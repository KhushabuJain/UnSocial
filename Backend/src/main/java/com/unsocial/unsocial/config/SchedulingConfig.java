package com.unsocial.unsocial.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables @Scheduled tasks across the application.
 * Used by SafetyTimerService to auto-expire timers every 30 seconds.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
