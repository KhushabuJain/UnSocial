package com.unsocial.unsocial.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String role;     // "USER" | "ASSISTANT"
    private String content;
    private LocalDateTime createdAt;
}
