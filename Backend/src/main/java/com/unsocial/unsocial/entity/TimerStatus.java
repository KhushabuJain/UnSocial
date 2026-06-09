package com.unsocial.unsocial.entity;

public enum TimerStatus {
    ACTIVE,
    COMPLETED,   // user checked in safely before expiry
    EXPIRED,     // timer ran out — SOS auto-triggered
    CANCELLED    // user manually cancelled
}
