package me.dohyeon.deepdive.domain.member.controller;

import jakarta.servlet.http.HttpServletResponse;
import me.dohyeon.deepdive.domain.member.dto.MemberResponse;
import me.dohyeon.deepdive.domain.member.dto.UpdateNicknameRequest;
import me.dohyeon.deepdive.domain.member.entity.Member;
import me.dohyeon.deepdive.domain.member.service.MemberService;
import me.dohyeon.deepdive.global.common.response.CommonResponse;
import me.dohyeon.deepdive.global.security.jwt.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MemberProfileController 단위 테스트
 *
 * <p>GET /api/v1/members/me - 내 정보 조회</p>
 */
@ExtendWith(MockitoExtension.class)
class MemberProfileControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private MemberProfileController memberProfileController;

    // ============================================================
    // GET /api/v1/members/me
    // ============================================================

    @Test
    @DisplayName("내 정보 조회 - memberId로 회원 정보를 반환한다")
    void getMe_success() {
        // given
        Long memberId = 1L;

        Member member = mock(Member.class);
        when(member.getId()).thenReturn(memberId);
        when(member.getEmail()).thenReturn("test@example.com");
        when(member.getNickname()).thenReturn("테스터");

        when(memberService.getMember(memberId)).thenReturn(member);

        // when
        CommonResponse<MemberResponse> response = memberProfileController.getMe(memberId);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.data().id()).isEqualTo(memberId);
        assertThat(response.data().email()).isEqualTo("test@example.com");
        assertThat(response.data().nickname()).isEqualTo("테스터");

        verify(memberService).getMember(memberId);
    }

    // ============================================================
    // PATCH /api/v1/members/me
    // ============================================================

    @Test
    @DisplayName("닉네임 수정 - 변경된 닉네임으로 회원 정보를 반환한다")
    void updateMe_success() {
        // given
        Long memberId = 1L;
        String newNickname = "새닉네임";
        UpdateNicknameRequest request = new UpdateNicknameRequest(newNickname);

        Member member = mock(Member.class);
        when(member.getId()).thenReturn(memberId);
        when(member.getEmail()).thenReturn("test@example.com");
        when(member.getNickname()).thenReturn(newNickname);

        when(memberService.updateNickname(memberId, newNickname)).thenReturn(member);

        // when
        CommonResponse<MemberResponse> response = memberProfileController.updateMe(memberId, request);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.data().nickname()).isEqualTo(newNickname);

        verify(memberService).updateNickname(memberId, newNickname);
    }

    // ============================================================
    // DELETE /api/v1/members/me
    // ============================================================

    @Test
    @DisplayName("회원 탈퇴 - 회원 삭제 후 AT/RT 쿠키가 만료된다")
    void deleteMe_success() {
        // given
        Long memberId = 1L;

        // when
        memberProfileController.deleteMe(memberId, response);

        // then
        verify(memberService).deleteMember(memberId);
        verify(cookieUtil).expireCookie(response, "accessToken");
        verify(cookieUtil).expireCookie(response, "refreshToken");
    }
}
