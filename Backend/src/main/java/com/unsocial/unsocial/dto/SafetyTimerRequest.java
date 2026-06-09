package com.unsocial.unsocial.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyTimerRequest {

    @NotNull(message = "Duration is required")
    @Min(value = 1,   message = "Duration must be at least 1 minute")
    @Max(value = 480, message = "Duration cannot exceed 8 hours (480 minutes)")
    private Integer durationMinutes;

    @Size(max = 200, message = "Note cannot exceed 200 characters")
    private String note;
}
