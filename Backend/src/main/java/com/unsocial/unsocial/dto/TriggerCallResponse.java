package com.unsocial.unsocial.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerCallResponse {
    private String callerName;
    private String callerPhone;
    private int delaySeconds;           // frontend counts down this many seconds, then rings
    private LocalDateTime triggeredAt;
    private String status;              // "IMMEDIATE" or "SCHEDULED"
}
