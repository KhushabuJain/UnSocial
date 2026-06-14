package com.unsocial.unsocial.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactRequest {

    @NotBlank(message = "Contact name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[+]?[0-9]{10}$",
            message = "Please provide a valid phone number"
    )
    private String phone;

    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Relationship is required")
    @Size(max = 50, message = "Relationship must be under 50 characters")
    private String relationship;

    private boolean isPrimary;

    private boolean notifyOnSos = true;
}
