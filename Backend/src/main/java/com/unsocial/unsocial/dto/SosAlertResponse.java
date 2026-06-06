package com.unsocial.unsocial.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unsocial.unsocial.entity.SosStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SosAlertResponse {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String address;
    private String message;
    private SosStatus status;
    private int contactsNotified;
    private String googleMapsLink;    // ready-to-use Maps link
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
