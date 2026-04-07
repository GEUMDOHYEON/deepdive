package me.dohyeon.deepdive.domain.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "답변 제출 결과")
public record AnswerSubmitResponse(

        @Schema(description = "세션 완료 여부")
        boolean sessionCompleted,

        @Schema(description = "세션 최종 점수 (sessionCompleted=true 일 때만 값이 있음)")
        Integer totalScore,

        @Schema(description = "AI 피드백")
        FeedbackResponse feedback,

        @Schema(description = "다음 질문 (sessionCompleted=true 이면 null)")
        NextQuestionResponse nextQuestion
) {

    @Schema(description = "AI 피드백 상세")
    public record FeedbackResponse(
            Long feedbackId,
            int scoreAccuracy,
            int scoreLogic,
            String feedbackComment,
            List<String> missingKeywords,
            String idealAnswer
    ) {
    }

    @Schema(description = "다음 면접 질문")
    public record NextQuestionResponse(
            Long questionId,
            String content,
            int sequence,
            boolean followUp
    ) {
    }
}
