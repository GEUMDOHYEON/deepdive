package me.dohyeon.deepdive.domain.interview.service;

import lombok.RequiredArgsConstructor;
import me.dohyeon.deepdive.domain.interview.dto.AnswerSubmitRequest;
import me.dohyeon.deepdive.domain.interview.dto.AnswerSubmitResponse;
import me.dohyeon.deepdive.domain.interview.dto.AnswerSubmitResponse.FeedbackResponse;
import me.dohyeon.deepdive.domain.interview.dto.AnswerSubmitResponse.NextQuestionResponse;
import me.dohyeon.deepdive.domain.interview.dto.AiEvaluationResponse;
import me.dohyeon.deepdive.domain.interview.dto.InProgressSessionResponse;
import me.dohyeon.deepdive.domain.interview.dto.InProgressSessionResponse.QuestionResponse;
import me.dohyeon.deepdive.domain.interview.dto.QnaResultDto;
import me.dohyeon.deepdive.domain.interview.dto.SessionResultResponse;
import me.dohyeon.deepdive.domain.interview.dto.SessionSummaryResponse;
import me.dohyeon.deepdive.domain.interview.dto.StartSessionRequest;
import me.dohyeon.deepdive.domain.interview.dto.StartSessionResponse;
import me.dohyeon.deepdive.domain.interview.entity.*;
import me.dohyeon.deepdive.domain.interview.repository.*;
import me.dohyeon.deepdive.domain.member.entity.Member;
import me.dohyeon.deepdive.domain.member.repository.MemberRepository;
import me.dohyeon.deepdive.global.error.BusinessException;
import me.dohyeon.deepdive.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewSessionService {

  private static final int MAX_QUESTIONS = 5;

  private final MemberRepository memberRepository;
  private final InterviewSessionRepository sessionRepository;
  private final InterviewQuestionRepository questionRepository;
  private final MemberAnswerRepository answerRepository;
  private final AiFeedbackRepository feedbackRepository;
  private final AiInterviewService aiInterviewService;

  public StartSessionResponse startSession(Long memberId, StartSessionRequest request) {
    Member member = findMember(memberId);
    InterviewCategory category = parseCategory(request.category());

    InterviewSession session = sessionRepository.save(
        InterviewSession.start(member, category)
    );

    String questionContent = aiInterviewService.generateFirstQuestion(category);
    InterviewQuestion question = questionRepository.save(
        InterviewQuestion.createFirst(session, member, questionContent)
    );

    return new StartSessionResponse(
        session.getId(),
        session.getCategory().name(),
        session.getStatus().name(),
        new StartSessionResponse.QuestionResponse(
            question.getId(),
            question.getContent(),
            question.getSequence()
        )
    );
  }

  /**
   * 지원자의 답변을 제출하고, AI 평가 및 다음 질문을 반환한다.
   *
   * <p>로직 흐름:</p>
   * <ol>
   *   <li>세션·질문 유효성 검증 (소유권, 완료 여부)</li>
   *   <li>MemberAnswer DB 저장</li>
   *   <li>AiInterviewService.evaluateAnswer 호출 → AiEvaluationResponse 획득</li>
   *   <li>AiFeedback DB 저장</li>
   *   <li>현재 세션의 질문 수 확인 → MAX_QUESTIONS(5)이면 세션 완료 처리</li>
   *   <li>질문이 남아있으면 다음 InterviewQuestion 생성 및 저장</li>
   *   <li>AnswerSubmitResponse 반환</li>
   * </ol>
   */
  public AnswerSubmitResponse submitAnswer(Long sessionId, Long questionId,
      Long memberId, AnswerSubmitRequest request) {
    // 1. 세션 검증
    InterviewSession session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

    if (!session.getMember().getId().equals(memberId)) {
      throw new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND);
    }

    if (session.getStatus() == InterviewStatus.COMPLETED) {
      throw new BusinessException(ErrorCode.INTERVIEW_SESSION_ALREADY_COMPLETED);
    }

    // 2. 질문 검증 (세션 소속 여부 포함)
    InterviewQuestion question = questionRepository.findByIdAndSessionId(questionId, sessionId)
        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND));

    Member member = session.getMember();

    // 3. 답변 저장
    MemberAnswer answer = answerRepository.save(
        MemberAnswer.create(questionId, sessionId, memberId,
            request.content(), request.processingTime())
    );

    // 4. AI 평가 (단일 호출로 평가 + 다음 질문 생성)
    AiEvaluationResponse evaluation = aiInterviewService.evaluateAnswer(
        question.getContent(), request.content()
    );

    // 5. 피드백 저장
    AiFeedback feedback = feedbackRepository.save(
        AiFeedback.create(answer.getId(), questionId, sessionId, memberId, evaluation)
    );

    FeedbackResponse feedbackResponse = toFeedbackResponse(feedback);

    // 6. 질문 수 기준으로 세션 완료 또는 다음 질문 생성 분기
    int questionCount = questionRepository.countBySessionId(sessionId);

    if (questionCount >= MAX_QUESTIONS) {
      // 마지막 답변: 세션 완료 처리 + 평균 점수 계산
      int totalScore = calculateAverageScore(feedbackRepository.findBySessionId(sessionId));
      session.complete(totalScore);

      return new AnswerSubmitResponse(true, totalScore, feedbackResponse, null);
    }

    // 아직 질문이 남아있으면 다음 질문 생성
    InterviewQuestion nextQuestion = questionRepository.save(
        InterviewQuestion.createNext(
            session, member,
            evaluation.nextQuestionContent(),
            questionCount + 1,
            evaluation.followUp()
        )
    );

    NextQuestionResponse nextQuestionResponse = new NextQuestionResponse(
        nextQuestion.getId(),
        nextQuestion.getContent(),
        nextQuestion.getSequence(),
        nextQuestion.isFollowUp()
    );

    return new AnswerSubmitResponse(false, null, feedbackResponse, nextQuestionResponse);
  }

  /**
   * 완료된 세션의 최종 리포트를 조회한다.
   *
   * <p>N+1 방지 전략: MemberAnswer·AiFeedback은 question_id를 JPA 연관관계 없이
   * plain Long으로 저장하므로, InterviewQuestion에서 JOIN FETCH를 할 수 없다. 대신 session_id 기준으로
   * 질문(1회)·답변(1회)·피드백(1회)을 일괄 조회한 뒤 questionId를 key로 하는 Map으로 변환하여 메모리에서 조립한다. 결과적으로 질문 N개에 관계없이 DB
   * 쿼리가 항상 3회로 고정된다.</p>
   *
   * @param memberId  요청자 ID (소유권 검증용)
   * @param sessionId 조회할 세션 ID
   * @return {@link SessionResultResponse}
   */
  @Transactional
  public SessionResultResponse getSessionReport(Long memberId, Long sessionId) {
    // 1. 세션 조회 (소유권 체크 포함)
    InterviewSession session = sessionRepository.findByIdAndMemberId(sessionId, memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

    // 3. N+1 방지: 질문·답변·피드백을 session 단위로 각 1회씩 일괄 조회
    List<InterviewQuestion> questions = questionRepository.findAllBySessionIdForReport(sessionId);

    // questionId → MemberAnswer 맵
    Map<Long, MemberAnswer> answerByQuestionId = answerRepository.findBySessionId(sessionId)
        .stream()
        .collect(Collectors.toMap(MemberAnswer::getQuestionId, a -> a));

    // questionId → AiFeedback 맵
    Map<Long, AiFeedback> feedbackByQuestionId = feedbackRepository.findBySessionId(sessionId)
        .stream()
        .collect(Collectors.toMap(AiFeedback::getQuestionId, f -> f));

    // 4. 메모리에서 QnaResultDto 조립
    List<QnaResultDto> qnaList = questions.stream()
        .map(q -> {
          MemberAnswer answer = answerByQuestionId.get(q.getId());
          AiFeedback feedback = feedbackByQuestionId.get(q.getId());
          return new QnaResultDto(
              q.getId(),
              q.getContent(),
              answer != null ? answer.getContent() : null,
              feedback != null ? feedback.getScoreAccuracy() : 0,
              feedback != null ? feedback.getScoreLogic() : 0,
              feedback != null ? feedback.getFeedbackComment() : null,
              feedback != null ? feedback.getMissingKeywords() : List.of(),
              feedback != null ? feedback.getIdealAnswer() : null
          );
        })
        .toList();

    // 5. 미완료 세션이거나 totalScore가 없으면 현재까지 피드백으로 점수 계산 후 완료 처리
    Integer totalScore = session.getTotalScore();
    if (session.getStatus() != InterviewStatus.COMPLETED || totalScore == null || totalScore == 0) {
      totalScore = calculateAverageScore(feedbackRepository.findBySessionId(sessionId));
      session.complete(totalScore);
    }

    return new SessionResultResponse(
        session.getId(),
        session.getCategory().name(),
        totalScore,
        session.getStatus().name(),
        qnaList
    );
  }

  private FeedbackResponse toFeedbackResponse(AiFeedback feedback) {
    return new FeedbackResponse(
        feedback.getId(),
        feedback.getScoreAccuracy(),
        feedback.getScoreLogic(),
        feedback.getFeedbackComment(),
        feedback.getMissingKeywords(),
        feedback.getIdealAnswer()
    );
  }

  /**
   * 세션 내 모든 피드백의 평균 점수를 계산한다. 점수 = (scoreAccuracy + scoreLogic) / 2 의 세션 전체 평균 (반올림)
   */
  private int calculateAverageScore(List<AiFeedback> feedbacks) {
    if (feedbacks.isEmpty()) {
      return 0;
    }
    double avg = feedbacks.stream()
        .mapToDouble(f -> (f.getScoreAccuracy() + f.getScoreLogic()) / 2.0)
        .average()
        .orElse(0.0);
    return (int) Math.round(avg);
  }

  private Member findMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
  }

  private InterviewCategory parseCategory(String category) {
    try {
      return InterviewCategory.valueOf(category.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.INTERVIEW_CATEGORY_INVALID);
    }
  }

  //    면접 세션 히스토리 조회 API
  @Transactional(readOnly = true)
  public List<SessionSummaryResponse> getSessionHistory(Long memberId) {
    return sessionRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId).stream()
        .map(s -> new SessionSummaryResponse(
            s.getId(),
            s.getCategory().name(),
            s.getStatus().name(),
            s.getTotalScore(),
            s.getCreatedAt()
        ))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<InProgressSessionResponse> getInProgressSessions(Long memberId) {
    return sessionRepository.findByMemberIdAndStatus(memberId,InterviewStatus.IN_PROGRESS).stream().map(session -> {
      InterviewQuestion currentQuestion =  questionRepository.findTopBySessionIdOrderBySequenceDesc(session.getId()).orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

      InProgressSessionResponse.QuestionResponse questionResponse = new QuestionResponse(
          currentQuestion.getId(),
          currentQuestion.getContent(),
          currentQuestion.getSequence(),
          currentQuestion.isFollowUp()
      );

      return new InProgressSessionResponse(
          session.getId(),
          session.getCategory().name(),
          questionResponse
      );
    }).toList();
  }
}
