package me.dohyeon.deepdive.domain.interview.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.dohyeon.deepdive.domain.member.entity.Member;
import me.dohyeon.deepdive.global.common.entity.BaseEntity;

@Entity
@Table(name = "interview_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSession extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private InterviewCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InterviewStatus status;

  @Column(name = "total_score")
  private Integer totalScore;

  @Builder
  private InterviewSession(Member member, InterviewCategory category) {
    this.member = member;
    this.category = category;
    this.status = InterviewStatus.IN_PROGRESS;
  }

  public static InterviewSession start(Member member, InterviewCategory category) {
    return InterviewSession.builder()
        .member(member)
        .category(category)
        .build();
  }

  public void complete(int totalScore) {
    this.status = InterviewStatus.COMPLETED;
    this.totalScore = totalScore;
  }
}
