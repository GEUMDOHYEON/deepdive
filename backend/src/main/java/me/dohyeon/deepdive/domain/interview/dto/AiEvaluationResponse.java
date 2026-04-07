package me.dohyeon.deepdive.domain.interview.dto;

import java.util.List;

/**
 * AI가 반환하는 평가 결과 JSON을 매핑하는 Record.
 * ObjectMapper로 역직렬화되므로 필드명이 JSON 키와 정확히 일치해야 한다.
 */
public record AiEvaluationResponse(
        int scoreAccuracy,
        int scoreLogic,
        String feedbackComment,
        List<String> missingKeywords,
        String idealAnswer,
        String nextQuestionContent,
        boolean followUp
) {
}
