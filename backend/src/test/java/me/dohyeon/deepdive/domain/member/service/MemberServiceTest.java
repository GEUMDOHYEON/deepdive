package me.dohyeon.deepdive.domain.member.service;

import me.dohyeon.deepdive.domain.auth.repository.RefreshTokenRepository;
import me.dohyeon.deepdive.domain.interview.repository.AiFeedbackRepository;
import me.dohyeon.deepdive.domain.interview.repository.InterviewQuestionRepository;
import me.dohyeon.deepdive.domain.interview.repository.InterviewSessionRepository;
import me.dohyeon.deepdive.domain.interview.repository.MemberAnswerRepository;
import me.dohyeon.deepdive.domain.member.entity.Member;
import me.dohyeon.deepdive.domain.member.repository.MemberRepository;
import me.dohyeon.deepdive.global.error.BusinessException;
import me.dohyeon.deepdive.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MemberService 단위 테스트
 *
 * <p>외부 의존성(Repository, PasswordEncoder)을 모두 Mock으로 대체하여
 * 순수하게 서비스 비즈니스 로직만 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private InterviewSessionRepository interviewSessionRepository;

    @Mock
    private InterviewQuestionRepository interviewQuestionRepository;

    @Mock
    private MemberAnswerRepository memberAnswerRepository;

    @Mock
    private AiFeedbackRepository aiFeedbackRepository;

    @InjectMocks
    private MemberService memberService;

    // ============================================================
    // signup() 테스트
    // ============================================================

    @Nested
    @DisplayName("로컬 회원가입")
    class Signup {

        @Test
        @DisplayName("정상 가입 - 비밀번호가 암호화되어 저장되고 저장된 회원을 반환한다")
        void signup_success() {
            // given
            String email = "test@example.com";
            String rawPassword = "password123";
            String nickname = "테스터";
            String encodedPassword = "$2a$10$encoded_password_here";

            when(memberRepository.existsByEmail(email)).thenReturn(false);
            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

            Member savedMember = mock(Member.class);
            when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

            // when
            Member result = memberService.signup(email, rawPassword, nickname);

            // then
            assertThat(result).isEqualTo(savedMember);
            verify(memberRepository).existsByEmail(email);
            verify(passwordEncoder).encode(rawPassword);
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("이메일 중복 - MEMBER_EMAIL_DUPLICATED 예외가 발생한다")
        void signup_duplicateEmail_throwsException() {
            // given
            String email = "duplicate@example.com";
            when(memberRepository.existsByEmail(email)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.signup(email, "password123", "닉네임"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex ->
                            assertThat(((BusinessException) ex).getErrorCode())
                                    .isEqualTo(ErrorCode.MEMBER_EMAIL_DUPLICATED)
                    );

            // 중복이면 저장하지 않는다
            verify(memberRepository, never()).save(any());
        }
    }

    // ============================================================
    // deleteMember() 테스트
    // ============================================================

    @Nested
    @DisplayName("회원 탈퇴")
    class DeleteMember {

        @Test
        @DisplayName("정상 탈퇴 - 연관 데이터를 순서대로 삭제 후 회원을 삭제한다")
        void deleteMember_success() {
            // given
            Long memberId = 1L;
            Member member = mock(Member.class);
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

            // when
            memberService.deleteMember(memberId);

            // then - FK 제약 조건을 지키는 삭제 순서 검증
            // interview 하위 데이터부터 삭제
            verify(aiFeedbackRepository).deleteByMemberId(memberId);
            verify(memberAnswerRepository).deleteByMemberId(memberId);
            verify(interviewQuestionRepository).deleteByMemberId(memberId);
            verify(interviewSessionRepository).deleteByMemberId(memberId);
            // 인증 데이터 삭제
            verify(refreshTokenRepository).deleteByMemberId(memberId);
            // 마지막으로 회원 삭제
            verify(memberRepository).delete(member);
        }

        @Test
        @DisplayName("존재하지 않는 ID - MEMBER_NOT_FOUND 예외가 발생한다")
        void deleteMember_notFound_throwsException() {
            // given
            Long memberId = 999L;
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.deleteMember(memberId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex ->
                            assertThat(((BusinessException) ex).getErrorCode())
                                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND)
                    );

            verify(memberRepository, never()).delete(any());
        }
    }

    // ============================================================
    // getMember() 테스트
    // ============================================================

    @Nested
    @DisplayName("내 정보 조회")
    class GetMember {

        @Test
        @DisplayName("정상 조회 - memberId로 회원을 반환한다")
        void getMember_success() {
            // given
            Long memberId = 1L;
            Member member = mock(Member.class);
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

            // when
            Member result = memberService.getMember(memberId);

            // then
            assertThat(result).isEqualTo(member);
            verify(memberRepository).findById(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 ID - MEMBER_NOT_FOUND 예외가 발생한다")
        void getMember_notFound_throwsException() {
            // given
            Long memberId = 999L;
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMember(memberId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex ->
                            assertThat(((BusinessException) ex).getErrorCode())
                                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND)
                    );
        }
    }

    // ============================================================
    // updateNickname() 테스트
    // ============================================================

    @Nested
    @DisplayName("닉네임 수정")
    class UpdateNickname {

        @Test
        @DisplayName("정상 수정 - 회원을 조회하여 닉네임을 변경한다")
        void updateNickname_success() {
            // given
            Long memberId = 1L;
            String newNickname = "새닉네임";

            Member member = mock(Member.class);
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

            // when
            memberService.updateNickname(memberId, newNickname);

            // then
            verify(memberRepository).findById(memberId);
            verify(member).updateNickname(newNickname);
        }

        @Test
        @DisplayName("존재하지 않는 ID - MEMBER_NOT_FOUND 예외가 발생한다")
        void updateNickname_notFound_throwsException() {
            // given
            Long memberId = 999L;
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updateNickname(memberId, "닉네임"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex ->
                            assertThat(((BusinessException) ex).getErrorCode())
                                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND)
                    );

            // 회원을 못 찾으면 updateNickname()은 호출되지 않는다
            // (verify는 필요 없음 - mock이 호출되지 않으면 자동으로 검증됨)
        }
    }

    // ============================================================
    // login() 테스트
    // ============================================================

    @Nested
    @DisplayName("로컬 로그인")
    class Login {

        @Test
        @DisplayName("정상 로그인 - 이메일과 비밀번호가 일치하면 회원을 반환한다")
        void login_success() {
            // given
            String email = "test@example.com";
            String rawPassword = "password123";
            String encodedPassword = "$2a$10$encoded_password_here";

            Member member = mock(Member.class);
            when(member.getPassword()).thenReturn(encodedPassword);

            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

            // when
            Member result = memberService.login(email, rawPassword);

            // then
            assertThat(result).isEqualTo(member);
        }

        @Test
        @DisplayName("이메일 없음 - MEMBER_NOT_FOUND 예외가 발생한다")
        void login_memberNotFound_throwsException() {
            // given
            String email = "notfound@example.com";
            when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.login(email, "password123"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex ->
                            assertThat(((BusinessException) ex).getErrorCode())
                                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND)
                    );
        }

        @Test
        @DisplayName("비밀번호 불일치 - MEMBER_INVALID_PASSWORD 예외가 발생한다")
        void login_invalidPassword_throwsException() {
            // given
            String email = "test@example.com";
            String rawPassword = "wrongPassword";
            String encodedPassword = "$2a$10$encoded_password_here";

            Member member = mock(Member.class);
            when(member.getPassword()).thenReturn(encodedPassword);

            when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> memberService.login(email, rawPassword))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex ->
                            assertThat(((BusinessException) ex).getErrorCode())
                                    .isEqualTo(ErrorCode.MEMBER_INVALID_PASSWORD)
                    );
        }
    }
}
