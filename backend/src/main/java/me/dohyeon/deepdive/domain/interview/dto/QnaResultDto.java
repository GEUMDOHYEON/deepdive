package me.dohyeon.deepdive.domain.interview.dto;

import java.util.List;

public record QnaResultDto(
        Long questionId,
        String questionContent,
        String userAnswer,
        int scoreAccuracy,
        int scoreLogic,
        String feedbackComment,
        List<String> missingKeywords,
        String idealAnswer
) {
}
