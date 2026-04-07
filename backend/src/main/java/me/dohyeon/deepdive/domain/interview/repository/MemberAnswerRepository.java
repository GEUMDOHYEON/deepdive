package me.dohyeon.deepdive.domain.interview.repository;

import me.dohyeon.deepdive.domain.interview.entity.MemberAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberAnswerRepository extends JpaRepository<MemberAnswer, Long> {

  List<MemberAnswer> findBySessionId(Long sessionId);

  void deleteByMemberId(Long memberId);

}
