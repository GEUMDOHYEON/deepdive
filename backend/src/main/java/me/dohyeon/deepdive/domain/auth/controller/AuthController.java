package me.dohyeon.deepdive.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.dohyeon.deepdive.domain.auth.service.AuthService;
import me.dohyeon.deepdive.global.common.response.CommonResponse;
import me.dohyeon.deepdive.global.error.BusinessException;
import me.dohyeon.deepdive.global.error.ErrorCode;
import me.dohyeon.deepdive.global.security.jwt.CookieUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "토큰 갱신 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final CookieUtil cookieUtil;

  /**
   * Access Token 갱신 (API 핑퐁 엔드포인트). 프론트엔드가 401 응답을 받은 뒤 이 API를 호출해 새 AT를 발급받습니다.
   */
  @Operation(
      summary = "Access Token 갱신",
      description = "refreshToken 쿠키를 검증하고 새로운 accessToken 쿠키를 발급합니다. " +
          "프론트엔드 인터셉터가 401 수신 시 자동으로 호출합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Access Token 갱신 성공"),
      @ApiResponse(responseCode = "401", description = "Refresh Token 없음 또는 유효하지 않음")
  })
  @PostMapping("/refresh")
  public CommonResponse<Void> refresh(
      @Parameter(hidden = true)
      @CookieValue(name = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response
  ) {
    if (refreshToken == null) {
      throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    String newAccessToken = authService.refresh(refreshToken);
    cookieUtil.addAccessTokenCookie(response, newAccessToken);
    return CommonResponse.ok();
  }

  @Operation(summary = "로그아웃", description = "RT를 삭제하고 AT/RT 쿠키를 만료시킵니다. 로그인이 필요합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  @PostMapping("/logout")
  public CommonResponse<Void> logout(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      HttpServletResponse response
  ) {
    authService.revokeRefreshToken(memberId);
    cookieUtil.expireCookie(response, "accessToken");
    cookieUtil.expireCookie(response, "refreshToken");
    return CommonResponse.ok();
  }
}
