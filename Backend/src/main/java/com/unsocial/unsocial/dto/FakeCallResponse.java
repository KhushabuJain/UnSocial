package com.unsocial.unsocial.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FakeCallResponse {
    private Long id;
    private String callerName;
    private String callerPhone;
    private int delaySeconds;
    private String ringtone;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
