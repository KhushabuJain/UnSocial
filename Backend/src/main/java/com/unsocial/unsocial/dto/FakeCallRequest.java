package com.unsocial.unsocial.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FakeCallRequest {

    @NotBlank(message = "Caller name is required")
    @Size(min = 1, max = 50, message = "Caller name must be between 1 and 50 characters")
    private String callerName;

    @Pattern(
            regexp = "^([+]?[0-9]{10,15})?$",
            message = "Invalid phone number format"
    )
    private String callerPhone;

    @Min(value = 0, message = "Delay cannot be negative")
    @Max(value = 3600, message = "Delay cannot exceed 1 hour (3600 seconds)")
    @Builder.Default
    private Integer delaySeconds = 0;

    @Builder.Default
    private boolean makeDefault = false;
}
