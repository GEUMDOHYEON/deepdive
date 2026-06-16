package me.dohyeon.deepdive.domain.interview.dto;

/**
 * AI 다음 질문 생성 전용 결과 (2개 필드).
 * generateNextQuestion() 호출 시 역직렬화 대상.
 */
public record AiNextQuestionResult(
        String nextQuestionContent,
        boolean followUp
) {
}
