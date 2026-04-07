package me.dohyeon.deepdive.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "")
public record SessionSummaryResponse(
    Long sessionId,
    String category,
    String status,
    Integer totalScore,
    LocalDateTime createdAt
) {

}
