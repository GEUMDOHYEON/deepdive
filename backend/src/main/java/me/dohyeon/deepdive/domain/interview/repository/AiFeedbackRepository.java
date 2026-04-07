package me.dohyeon.deepdive.domain.interview.repository;

import me.dohyeon.deepdive.domain.interview.entity.AiFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, Long> {

  List<AiFeedback> findBySessionId(Long sessionId);

  void deleteByMemberId(Long memberId);
}
