package me.dohyeon.deepdive.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import me.dohyeon.deepdive.domain.auth.service.AuthService;
import me.dohyeon.deepdive.global.security.jwt.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * AuthController 단위 테스트
 *
 * <p>Spring Context 없이 Controller 메서드를 직접 호출하여
 * 비즈니스 흐름(서비스 호출, 쿠키 처리)만 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    // ============================================================
    // POST /api/v1/auth/logout
    // ============================================================

    @Test
    @DisplayName("로그아웃 - RT 삭제 및 AT/RT 쿠키가 만료된다")
    void logout_success() {
        // given
        Long memberId = 1L;

        // when
        authController.logout(memberId, response);

        // then - RT가 삭제되어야 한다
        verify(authService).revokeRefreshToken(memberId);

        // then - AT, RT 쿠키 모두 만료되어야 한다
        verify(cookieUtil).expireCookie(response, "accessToken");
        verify(cookieUtil).expireCookie(response, "refreshToken");
    }
}
