package com.unsocial.unsocial.dto;

import com.unsocial.unsocial.entity.MessageType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerMessageResponse {
    private String senderName;
    private String senderPhone;
    private String messageContent;
    private MessageType messageType;
    private LocalDateTime triggeredAt;
}
