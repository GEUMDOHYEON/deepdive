package me.dohyeon.deepdive.domain.member.repository;

import me.dohyeon.deepdive.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  Optional<Member> findBySocialIdAndSocialProvider(String socialId, String socialProvider);

  boolean existsByEmail(String email);

  boolean existsBySocialId(String socialId);
  
}
