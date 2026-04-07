package me.dohyeon.deepdive.domain.interview.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.dohyeon.deepdive.domain.interview.dto.AiEvaluationResponse;
import me.dohyeon.deepdive.global.common.converter.StringListConverter;
import me.dohyeon.deepdive.global.common.entity.BaseEntity;

import java.util.List;

@Entity
@Table(name = "ai_feedback")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_id", nullable = false)
    private Long answerId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "score_accuracy", nullable = false)
    private int scoreAccuracy;

    @Column(name = "score_logic", nullable = false)
    private int scoreLogic;

    @Column(name = "feedback_comment", nullable = false, columnDefinition = "TEXT")
    private String feedbackComment;

    @Convert(converter = StringListConverter.class)
    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private List<String> missingKeywords;

    @Column(name = "ideal_answer", nullable = false, columnDefinition = "TEXT")
    private String idealAnswer;

    @Builder
    private AiFeedback(Long answerId, Long questionId, Long sessionId, Long memberId,
                       int scoreAccuracy, int scoreLogic, String feedbackComment,
                       List<String> missingKeywords, String idealAnswer) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.sessionId = sessionId;
        this.memberId = memberId;
        this.scoreAccuracy = scoreAccuracy;
        this.scoreLogic = scoreLogic;
        this.feedbackComment = feedbackComment;
        this.missingKeywords = missingKeywords;
        this.idealAnswer = idealAnswer;
    }

    public static AiFeedback create(Long answerId, Long questionId, Long sessionId,
                                    Long memberId, AiEvaluationResponse evaluation) {
        return AiFeedback.builder()
                .answerId(answerId)
                .questionId(questionId)
                .sessionId(sessionId)
                .memberId(memberId)
                .scoreAccuracy(evaluation.scoreAccuracy())
                .scoreLogic(evaluation.scoreLogic())
                .feedbackComment(evaluation.feedbackComment())
                .missingKeywords(evaluation.missingKeywords())
                .idealAnswer(evaluation.idealAnswer())
                .build();
    }
}
