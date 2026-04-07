package me.dohyeon.deepdive.domain.interview.dto;

import java.util.List;

public record SessionResultResponse(
        Long sessionId,
        String category,
        Integer totalScore,
        String status,
        List<QnaResultDto> qnaList
) {
}
