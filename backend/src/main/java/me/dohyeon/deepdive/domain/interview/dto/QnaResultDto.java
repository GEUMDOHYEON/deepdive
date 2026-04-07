package me.dohyeon.deepdive.domain.interview.dto;

public record QnaResultDto(
        Long questionId,
        String questionContent,
        String userAnswer,
        int scoreAccuracy,
        int scoreLogic,
        String feedbackComment,
        String idealAnswer
) {
}
