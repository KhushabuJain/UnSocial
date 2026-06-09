package com.unsocial.unsocial.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerMessageRequest {

    // If null, user's default template is used
    private Long templateId;

    // Optionally override the template's message text
    @Size(max = 1000, message = "Override content cannot exceed 1000 characters")
    private String overrideContent;
}
