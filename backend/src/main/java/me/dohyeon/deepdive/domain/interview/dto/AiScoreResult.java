package me.dohyeon.deepdive.domain.interview.dto;

import java.util.List;

/**
 * AI 점수·키워드 전용 결과 (3개 필드, 숫자+짧은 목록 → 빠름).
 * evaluateScores() 호출 시 역직렬화 대상.
 */
public record AiScoreResult(
        int scoreAccuracy,
        int scoreLogic,
        List<String> missingKeywords
) {
}
