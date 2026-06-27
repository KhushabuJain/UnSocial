package com.unsocial.unsocial.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAnswerResponse {
    private String reply;
    private List<String> sources;   // knowledge-base filenames used to ground this answer
    private LocalDateTime createdAt;
}
