package me.dohyeon.deepdive.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.dohyeon.deepdive.domain.auth.dto.TokenPair;
import me.dohyeon.deepdive.domain.auth.service.AuthService;
import me.dohyeon.deepdive.domain.member.entity.Member;
import me.dohyeon.deepdive.domain.member.repository.MemberRepository;
import me.dohyeon.deepdive.global.security.jwt.CookieUtil;
import me.dohyeon.deepdive.global.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend.main-url}")
    private String mainUrl;

    @Value("${app.frontend.signup-url}")
    private String signupUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Optional<Member> existingMember = memberRepository.findBySocialIdAndSocialProvider(
                oAuth2User.getSocialId(), oAuth2User.getProvider()
        );

        if (existingMember.isPresent()) {
            // 기존 회원: AT + RT 발급 후 메인 페이지로 리다이렉트
            TokenPair tokens = authService.issueTokens(existingMember.get().getId());
            cookieUtil.addAccessTokenCookie(response, tokens.accessToken());
            cookieUtil.addRefreshTokenCookie(response, tokens.refreshToken());
            response.sendRedirect(mainUrl);
        } else {
            // 신규 유저: 임시 회원가입 토큰 발급 후 /signup으로 리다이렉트 (DB 저장 없음)
            String signupToken = jwtProvider.generateSignupToken(
                    oAuth2User.getSocialId(), oAuth2User.getProvider(), oAuth2User.getEmail()
            );
            int maxAge = (int) (jwtProvider.getSignupTokenExpiration() / 1000);
            ResponseCookie cookie = ResponseCookie.from("signupToken", signupToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(maxAge)
                    .sameSite("Lax")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());
            response.sendRedirect(signupUrl);
        }
    }
}
