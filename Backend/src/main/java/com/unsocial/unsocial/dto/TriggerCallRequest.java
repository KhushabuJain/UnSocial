package com.unsocial.unsocial.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerCallRequest {

    // If null, the user's default template is used
    private Long templateId;

    // Override the template's stored delay (optional)
    @Min(value = 0, message = "Delay cannot be negative")
    @Max(value = 3600, message = "Delay cannot exceed 1 hour")
    private Integer overrideDelaySeconds;
}
