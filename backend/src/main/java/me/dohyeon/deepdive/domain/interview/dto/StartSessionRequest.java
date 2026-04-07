package me.dohyeon.deepdive.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "면접 세션 시작 요청")
public record StartSessionRequest(
        @Schema(
                description = "면접 카테고리 (OS, SECURITY, DATABASE, DATA_STRUCTURE, NETWORK, SOFTWARE_DESIGN)",
                example = "OS"
        ) String category
) {
}
