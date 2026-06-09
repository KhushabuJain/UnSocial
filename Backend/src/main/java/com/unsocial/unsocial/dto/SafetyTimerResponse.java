package com.unsocial.unsocial.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unsocial.unsocial.entity.TimerStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SafetyTimerResponse {
    private Long id;
    private int durationMinutes;
    private String note;
    private TimerStatus status;
    private LocalDateTime expiresAt;
    private long remainingSeconds;   // calculated at response time — frontend uses for countdown
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
