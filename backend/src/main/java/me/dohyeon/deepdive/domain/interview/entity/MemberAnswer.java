package me.dohyeon.deepdive.domain.interview.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.dohyeon.deepdive.global.common.entity.BaseEntity;

@Entity
@Table(name = "member_answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "processing_time")
    private Integer processingTime;

    @Builder
    private MemberAnswer(Long questionId, Long sessionId, Long memberId,
                         String content, Integer processingTime) {
        this.questionId = questionId;
        this.sessionId = sessionId;
        this.memberId = memberId;
        this.content = content;
        this.processingTime = processingTime;
    }

    public static MemberAnswer create(Long questionId, Long sessionId, Long memberId,
                                      String content, Integer processingTime) {
        return MemberAnswer.builder()
                .questionId(questionId)
                .sessionId(sessionId)
                .memberId(memberId)
                .content(content)
                .processingTime(processingTime)
                .build();
    }
}
