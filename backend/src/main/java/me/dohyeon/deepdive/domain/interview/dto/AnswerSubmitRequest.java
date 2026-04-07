package me.dohyeon.deepdive.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "답변 제출 요청")
public record AnswerSubmitRequest(

    @Schema(description = "사용자 답변 내용")
    String content,

    @Schema(description = "답변 작성 소요 시간 (초)")
    Integer processingTime
) {

}
