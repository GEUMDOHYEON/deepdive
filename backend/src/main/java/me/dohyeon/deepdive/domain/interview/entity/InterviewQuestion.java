package me.dohyeon.deepdive.domain.interview.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.dohyeon.deepdive.domain.member.entity.Member;

@Entity
@Table(name = "interview_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private InterviewSession session;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private Integer sequence;

  @Column(name = "is_follow_up", nullable = false)
  private boolean followUp;

  @Builder
  private InterviewQuestion(InterviewSession session, Member member, String content,
      int sequence, boolean followUp) {
    this.session = session;
    this.member = member;
    this.content = content;
    this.sequence = sequence;
    this.followUp = followUp;
  }

  public static InterviewQuestion createFirst(InterviewSession session, Member member,
      String content) {
    return InterviewQuestion.builder()
        .session(session)
        .member(member)
        .content(content)
        .sequence(1)
        .followUp(false)
        .build();
  }

  public static InterviewQuestion createNext(InterviewSession session, Member member,
      String content, int sequence, boolean followUp) {
    return InterviewQuestion.builder()
        .session(session)
        .member(member)
        .content(content)
        .sequence(sequence)
        .followUp(followUp)
        .build();
  }
}
