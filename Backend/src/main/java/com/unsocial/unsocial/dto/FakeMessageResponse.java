package com.unsocial.unsocial.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unsocial.unsocial.entity.MessageType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FakeMessageResponse {
    private Long id;
    private String senderName;
    private String senderPhone;
    private String messageContent;
    private MessageType messageType;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
