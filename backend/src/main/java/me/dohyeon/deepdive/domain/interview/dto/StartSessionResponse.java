package me.dohyeon.deepdive.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "면접 세션 시작 응답")
public record StartSessionResponse(
        @Schema(description = "세션 ID", example = "1") Long sessionId,
        @Schema(description = "카테고리", example = "OS") String category,
        @Schema(description = "세션 상태", example = "IN_PROGRESS") String status,
        @Schema(description = "첫 번째 질문") QuestionResponse firstQuestion
) {
    @Schema(description = "질문 정보")
    public record QuestionResponse(
            @Schema(description = "질문 ID", example = "1") Long questionId,
            @Schema(description = "질문 내용", example = "프로세스와 스레드의 차이점을 설명해주세요.") String content,
            @Schema(description = "질문 순서", example = "1") int sequence
    ) {
    }
}
