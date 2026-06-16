package me.dohyeon.deepdive.domain.interview.dto;

/**
 * AI 피드백 텍스트 전용 결과 (2개 필드, 긴 텍스트 → 느림).
 * generateFeedbackText() 호출 시 역직렬화 대상.
 */
public record AiFeedbackTextResult(
        String feedbackComment,
        String idealAnswer
) {
}
