package com.unsocial.unsocial.dto;

import com.unsocial.unsocial.entity.MessageType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FakeMessageRequest {

    @NotBlank(message = "Sender name is required")
    @Size(max = 50, message = "Sender name must be under 50 characters")
    private String senderName;

    @Pattern(regexp = "^([+]?[0-9]{10})?$", message = "Invalid phone number")
    private String senderPhone;

    @NotBlank(message = "Message content is required")
    @Size(min = 5, max = 1000, message = "Message must be between 5 and 1000 characters")
    private String messageContent;

    @Builder.Default
    private MessageType messageType = MessageType.CUSTOM;

    @Builder.Default
    private boolean makeDefault = false;
}
