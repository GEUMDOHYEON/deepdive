package me.dohyeon.deepdive.global.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * HttpOnly 쿠키 생성/만료 유틸리티. AT는 path="/", RT는 갱신 엔드포인트 경로에만 전송되도록 제한합니다.
 */
@Component
public class CookieUtil {

  private final boolean secure;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public CookieUtil(
      @Value("${app.cookie.secure:false}") boolean secure,
      @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpiration
  ) {
    this.secure = secure;
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  public void addAccessTokenCookie(HttpServletResponse response, String token) {
    addCookie(response, "accessToken", token, "/", (int) (accessTokenExpiration / 1000));
  }

  public void addRefreshTokenCookie(HttpServletResponse response, String token) {
    addCookie(response, "refreshToken", token, "/api/v1/auth/refresh",
        (int) (refreshTokenExpiration / 1000));
  }

  public void expireCookie(HttpServletResponse response, String name) {
    addCookie(response, name, "", "/", 0);
  }

  private void addCookie(HttpServletResponse response, String name, String value, String path,
      int maxAge) {
    ResponseCookie cookie = ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(secure)
        .path(path)
        .maxAge(maxAge)
        .sameSite("Lax")
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }
}
