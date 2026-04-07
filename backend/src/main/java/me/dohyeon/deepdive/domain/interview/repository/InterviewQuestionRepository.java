package me.dohyeon.deepdive.domain.interview.repository;

import me.dohyeon.deepdive.domain.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

  List<InterviewQuestion> findBySessionIdOrderBySequenceAsc(Long sessionId);

  Optional<InterviewQuestion> findBySessionIdAndSequence(Long sessionId, int sequence);

  Optional<InterviewQuestion> findByIdAndSessionId(Long id, Long sessionId);

  int countBySessionId(Long sessionId);

  void deleteByMemberId(Long memberId);


  /**
   * 리포트 조회용: 세션의 모든 질문을 sequence 순으로 조회한다.
   *
   * <p>MemberAnswer·AiFeedback은 question_id를 plain Long으로 저장하기 때문에
   * JPQL JOIN FETCH로 한 번에 끌어올 수 없다. 대신 이 메서드로 질문을 일괄 조회하고, 답변·피드백은 session_id 기준으로 각각 1회씩 별도 조회한 뒤
   * Map으로 조립한다. 결과적으로 질문 N개에 대해 쿼리가 3회로 고정되어 N+1 문제를 방지한다.</p>
   */
  @Query("SELECT q FROM InterviewQuestion q WHERE q.session.id = :sessionId ORDER BY q.sequence ASC")
  List<InterviewQuestion> findAllBySessionIdForReport(@Param("sessionId") Long sessionId);
}
