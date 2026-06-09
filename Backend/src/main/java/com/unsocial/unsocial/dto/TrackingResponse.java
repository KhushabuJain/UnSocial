package com.unsocial.unsocial.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unsocial.unsocial.entity.TrackingStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackingResponse {
    private Long id;
    private Double startLatitude;
    private Double startLongitude;
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentAddress;
    private String shareToken;
    private String shareLink;       // full URL to share with contacts
    private String googleMapsLink;  // direct Google Maps link
    private TrackingStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime stoppedAt;
}
