package me.dohyeon.deepdive.domain.interview.service;

import me.dohyeon.deepdive.domain.interview.dto.InProgressSessionResponse;
import me.dohyeon.deepdive.domain.interview.dto.SessionSummaryResponse;
import me.dohyeon.deepdive.domain.interview.entity.InterviewCategory;
import me.dohyeon.deepdive.domain.interview.entity.InterviewQuestion;
import me.dohyeon.deepdive.domain.interview.entity.InterviewSession;
import me.dohyeon.deepdive.domain.interview.entity.InterviewStatus;
import me.dohyeon.deepdive.domain.interview.repository.AiFeedbackRepository;
import me.dohyeon.deepdive.domain.interview.repository.InterviewQuestionRepository;
import me.dohyeon.deepdive.domain.interview.repository.InterviewSessionRepository;
import me.dohyeon.deepdive.domain.interview.repository.MemberAnswerRepository;
import me.dohyeon.deepdive.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * InterviewSessionService 단위 테스트
 *
 * <p>외부 의존성(Repository, AI)을 모두 Mock으로 대체하여
 * 순수하게 서비스 비즈니스 로직만 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
class InterviewSessionServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private InterviewSessionRepository sessionRepository;

  @Mock
  private InterviewQuestionRepository questionRepository;

  @Mock
  private MemberAnswerRepository answerRepository;

  @Mock
  private AiFeedbackRepository feedbackRepository;

  @Mock
  private AiInterviewService aiInterviewService;

  @InjectMocks
  private InterviewSessionService interviewSessionService;

  // ============================================================
  // getInProgressSessions() 테스트
  // ============================================================

  @Test
  @DisplayName("진행 중 세션 조회 - 세션과 현재 질문(마지막 sequence)을 반환한다")
  void getInProgressSessions_returnsSessions() {
    // given
    Long memberId = 1L;

    InterviewSession session = mock(InterviewSession.class);
    when(session.getId()).thenReturn(10L);
    when(session.getCategory()).thenReturn(InterviewCategory.OS);

    InterviewQuestion currentQuestion = mock(InterviewQuestion.class);
    when(currentQuestion.getId()).thenReturn(3L);
    when(currentQuestion.getContent()).thenReturn("프로세스와 스레드의 차이를 설명해주세요.");
    when(currentQuestion.getSequence()).thenReturn(3);

    when(sessionRepository.findByMemberIdAndStatus(memberId, InterviewStatus.IN_PROGRESS))
        .thenReturn(List.of(session));
    when(questionRepository.findTopBySessionIdOrderBySequenceDesc(10L))
        .thenReturn(Optional.of(currentQuestion));

    // when
    List<InProgressSessionResponse> result =
        interviewSessionService.getInProgressSessions(memberId);

    // then
    assertThat(result).hasSize(1);

    InProgressSessionResponse response = result.get(0);
    assertThat(response.sessionId()).isEqualTo(10L);
    assertThat(response.category()).isEqualTo("OS");
    assertThat(response.currentQuestion().questionId()).isEqualTo(3L);
    assertThat(response.currentQuestion().sequence()).isEqualTo(3);
  }

  @Test
  @DisplayName("진행 중 세션 조회 - 진행 중 세션이 없으면 빈 리스트를 반환한다")
  void getInProgressSessions_emptyList() {
    // given
    Long memberId = 1L;
    when(sessionRepository.findByMemberIdAndStatus(memberId, InterviewStatus.IN_PROGRESS))
        .thenReturn(Collections.emptyList());

    // when
    List<InProgressSessionResponse> result =
        interviewSessionService.getInProgressSessions(memberId);

    // then
    assertThat(result).isEmpty();
  }

  // ============================================================
  // getSessionHistory() 테스트
  // ============================================================

  @Test
  @DisplayName("세션 히스토리 조회 - 완료·진행 중 세션이 DB 반환 순서대로 응답에 담긴다")
  void getSessionHistory_returnsSessions() {
    // given
    Long memberId = 1L;

    InterviewSession completed = mock(InterviewSession.class);
    when(completed.getId()).thenReturn(10L);
    when(completed.getCategory()).thenReturn(InterviewCategory.OS);
    when(completed.getStatus()).thenReturn(InterviewStatus.COMPLETED);
    when(completed.getTotalScore()).thenReturn(82);
    when(completed.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 3, 20, 10, 0));

    InterviewSession inProgress = mock(InterviewSession.class);
    when(inProgress.getId()).thenReturn(11L);
    when(inProgress.getCategory()).thenReturn(InterviewCategory.NETWORK);
    when(inProgress.getStatus()).thenReturn(InterviewStatus.IN_PROGRESS);
    when(inProgress.getTotalScore()).thenReturn(null);
    when(inProgress.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 3, 18, 9, 0));

    // Repository가 최신순 정렬 결과를 반환한다고 가정
    when(sessionRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId))
        .thenReturn(List.of(completed, inProgress));

    // when
    List<SessionSummaryResponse> result = interviewSessionService.getSessionHistory(memberId);

    // then
    assertThat(result).hasSize(2);

    SessionSummaryResponse first = result.get(0);
    assertThat(first.sessionId()).isEqualTo(10L);
    assertThat(first.category()).isEqualTo("OS");
    assertThat(first.status()).isEqualTo("COMPLETED");
    assertThat(first.totalScore()).isEqualTo(82);
    assertThat(first.createdAt()).isEqualTo(LocalDateTime.of(2025, 3, 20, 10, 0));

    SessionSummaryResponse second = result.get(1);
    assertThat(second.sessionId()).isEqualTo(11L);
    assertThat(second.status()).isEqualTo("IN_PROGRESS");
    assertThat(second.totalScore()).isNull();  // 진행 중이면 점수 없음

    verify(sessionRepository).findAllByMemberIdOrderByCreatedAtDesc(memberId);
  }

  @Test
  @DisplayName("세션 히스토리 조회 - 세션이 하나도 없으면 빈 리스트를 반환한다")
  void getSessionHistory_emptyList() {
    // given
    Long memberId = 99L;
    when(sessionRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId))
        .thenReturn(Collections.emptyList());

    // when
    List<SessionSummaryResponse> result = interviewSessionService.getSessionHistory(memberId);

    // then
    assertThat(result).isEmpty();
  }
}
