package me.dohyeon.deepdive.domain.interview.repository;

import java.time.LocalDateTime;
import me.dohyeon.deepdive.domain.interview.entity.InterviewSession;
import me.dohyeon.deepdive.domain.interview.entity.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

  List<InterviewSession> findByMemberId(Long memberId);

  Optional<InterviewSession> findByIdAndMemberId(Long id, Long memberId);

  List<InterviewSession> findByMemberIdAndStatus(Long memberId, InterviewStatus status);

  List<InterviewSession> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

  void deleteByMemberId(Long memberId);

}
