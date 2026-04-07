package me.dohyeon.deepdive.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.dohyeon.deepdive.domain.auth.dto.TokenPair;
import me.dohyeon.deepdive.domain.auth.service.AuthService;
import me.dohyeon.deepdive.domain.member.dto.GoogleSignupRequest;
import me.dohyeon.deepdive.domain.member.dto.LocalLoginRequest;
import me.dohyeon.deepdive.domain.member.dto.LocalSignupRequest;
import me.dohyeon.deepdive.domain.member.dto.MemberResponse;
import me.dohyeon.deepdive.domain.member.entity.Member;
import me.dohyeon.deepdive.domain.member.service.MemberService;
import me.dohyeon.deepdive.global.common.response.CommonResponse;
import me.dohyeon.deepdive.global.error.BusinessException;
import me.dohyeon.deepdive.global.error.ErrorCode;
import me.dohyeon.deepdive.global.security.jwt.CookieUtil;
import me.dohyeon.deepdive.global.security.jwt.JwtProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;
  private final AuthService authService;
  private final JwtProvider jwtProvider;
  private final CookieUtil cookieUtil;

  @Operation(summary = "로컬 회원가입", description = "이메일/비밀번호/닉네임으로 회원가입하고 AT + RT 쿠키를 발급합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "회원가입 성공"),
      @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일 또는 닉네임")
  })
  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public CommonResponse<MemberResponse> signup(
      @RequestBody LocalSignupRequest request,
      HttpServletResponse response
  ) {
    Member member = memberService.signup(request.email(), request.password(), request.nickname());
    issueTokenCookies(member.getId(), response);
    return CommonResponse.ok(MemberResponse.from(member));
  }

  @Operation(summary = "로컬 로그인", description = "이메일/비밀번호로 로그인하고 AT + RT 쿠키를 발급합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
  })
  @PostMapping("/login")
  public CommonResponse<MemberResponse> login(
      @RequestBody LocalLoginRequest request,
      HttpServletResponse response
  ) {
    Member member = memberService.login(request.email(), request.password());
    issueTokenCookies(member.getId(), response);
    return CommonResponse.ok(MemberResponse.from(member));
  }

  @Operation(
      summary = "구글 회원가입 완료",
      description = "구글 OAuth2 인증 후 발급된 signupToken 쿠키와 닉네임으로 회원가입을 완료합니다. " +
          "성공 시 signupToken은 만료되고 AT + RT가 발급됩니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "구글 회원가입 성공"),
      @ApiResponse(responseCode = "400", description = "signupToken 누락 또는 유효하지 않음"),
      @ApiResponse(responseCode = "409", description = "이미 가입된 구글 계정 또는 닉네임 중복")
  })
  @PostMapping("/signup/google")
  @ResponseStatus(HttpStatus.CREATED)
  public CommonResponse<MemberResponse> googleSignup(
      @RequestBody GoogleSignupRequest request,
      @Parameter(hidden = true) @CookieValue(name = "signupToken") String signupToken,
      HttpServletResponse response
  ) {
    if (!jwtProvider.validateToken(signupToken)) {
      throw new BusinessException(ErrorCode.SIGNUP_TOKEN_INVALID);
    }

    var claims = jwtProvider.getClaims(signupToken);
    if (!"signup".equals(claims.get("type"))) {
      throw new BusinessException(ErrorCode.SIGNUP_TOKEN_INVALID);
    }

    String socialId = claims.getSubject();
    String email = claims.get("email", String.class);

    Member member = memberService.googleSignup(socialId, email, request.nickname());

    cookieUtil.expireCookie(response, "signupToken");
    issueTokenCookies(member.getId(), response);
    return CommonResponse.ok(MemberResponse.from(member));
  }

  private void issueTokenCookies(Long memberId, HttpServletResponse response) {
    TokenPair tokens = authService.issueTokens(memberId);
    cookieUtil.addAccessTokenCookie(response, tokens.accessToken());
    cookieUtil.addRefreshTokenCookie(response, tokens.refreshToken());
  }
}
