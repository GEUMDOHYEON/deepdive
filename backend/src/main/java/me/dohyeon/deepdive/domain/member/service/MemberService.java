package me.dohyeon.deepdive.domain.member.service;

import lombok.RequiredArgsConstructor;
import me.dohyeon.deepdive.domain.auth.repository.RefreshTokenRepository;
import me.dohyeon.deepdive.domain.interview.repository.AiFeedbackRepository;
import me.dohyeon.deepdive.domain.interview.repository.InterviewQuestionRepository;
import me.dohyeon.deepdive.domain.interview.repository.InterviewSessionRepository;
import me.dohyeon.deepdive.domain.interview.repository.MemberAnswerRepository;
import me.dohyeon.deepdive.domain.member.entity.Member;
import me.dohyeon.deepdive.domain.member.repository.MemberRepository;
import me.dohyeon.deepdive.global.error.BusinessException;
import me.dohyeon.deepdive.global.error.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final AiFeedbackRepository aiFeedbackRepository;
  private final MemberAnswerRepository memberAnswerRepository;
  private final InterviewSessionRepository interviewSessionRepository;
  private final InterviewQuestionRepository interviewQuestionRepository;
  private final RefreshTokenRepository refreshTokenRepository;

  /**
   * 로컬 회원가입. 이메일 중복 검사 후 비밀번호를 암호화하여 저장한다.
   */
  public Member signup(String email, String password, String nickname) {
    if (memberRepository.existsByEmail(email)) {
      throw new BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
    }
    return memberRepository.save(
        Member.createLocal(email, passwordEncoder.encode(password), nickname)
    );
  }

  /**
   * 로컬 로그인. 이메일로 회원을 조회하고 비밀번호를 검증한다.
   */
  @Transactional(readOnly = true)
  public Member login(String email, String password) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new BusinessException(ErrorCode.MEMBER_INVALID_PASSWORD);
    }
    return member;
  }

  /**
   * 구글 회원가입 완료. OAuth2 플로우에서 발급된 signupToken을 검증한 후 호출된다. 이메일 중복 검사 후 소셜 정보를 포함하여 저장한다.
   */
  public Member googleSignup(String socialId, String email, String nickname) {
    if (memberRepository.existsByEmail(email)) {
      throw new BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
    }
    return memberRepository.save(
        Member.createGoogle(email, nickname, socialId)
    );
  }

  @Transactional(readOnly = true)
  public Member getMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
  }

  public Member updateNickname(Long memberId, String nickname) {
    Member member = getMember(memberId);
    member.updateNickname(nickname);
    return member;
  }

  public void deleteMember(Long memberId) {
    Member member = getMember(memberId);

//    1. FK 부터 삭제
    aiFeedbackRepository.deleteByMemberId(memberId);
    memberAnswerRepository.deleteByMemberId(memberId);
    interviewQuestionRepository.deleteByMemberId(memberId);
    interviewSessionRepository.deleteByMemberId(memberId);
//    2. 인증 데이터 삭제
    refreshTokenRepository.deleteByMemberId(memberId);
//    3. 회원 삭제
    memberRepository.delete(member);
  }
}
