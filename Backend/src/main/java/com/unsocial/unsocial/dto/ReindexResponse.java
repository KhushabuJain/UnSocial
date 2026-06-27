package com.unsocial.unsocial.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReindexResponse {
    private int chunksIndexed;
    private long durationMs;
}
