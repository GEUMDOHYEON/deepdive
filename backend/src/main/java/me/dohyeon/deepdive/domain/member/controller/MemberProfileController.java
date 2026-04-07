package me.dohyeon.deepdive.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.dohyeon.deepdive.domain.member.dto.MemberResponse;
import me.dohyeon.deepdive.domain.member.dto.UpdateNicknameRequest;
import me.dohyeon.deepdive.domain.member.entity.Member;
import me.dohyeon.deepdive.domain.member.service.MemberService;
import me.dohyeon.deepdive.global.common.response.CommonResponse;
import me.dohyeon.deepdive.global.security.jwt.CookieUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "회원 프로필 관리 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberProfileController {

  private final MemberService memberService;
  private final CookieUtil cookieUtil;

  @Operation(summary = "내 정보 조회", description = "로그인한 회원의 정보를 반환합니다. 로그인이 필요합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증 필요")
  })
  @GetMapping("/me")
  public CommonResponse<MemberResponse> getMe(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
  ) {
    Member member = memberService.getMember(memberId);
    MemberResponse memberResponse = MemberResponse.from(member);
    return CommonResponse.ok(memberResponse);
  }

  @Operation(summary = "닉네임 수정", description = "로그인한 회원의 닉네임을 변경합니다. 로그인이 필요합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "401", description = "인증 필요"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
  })
  @PatchMapping("/me")
  public CommonResponse<MemberResponse> updateMe(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      @RequestBody UpdateNicknameRequest request
  ) {
    Member member = memberService.updateNickname(memberId, request.nickname());
    MemberResponse memberResponse = MemberResponse.from(member);
    return CommonResponse.ok(memberResponse);
  }

  @Operation(summary = "회원 탈퇴", description = "회원 및 모든 연관 데이터를 삭제하고 쿠키를 만료시킵니다. 로그인이 필요합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
      @ApiResponse(responseCode = "401", description = "인증 필요"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
  })
  @DeleteMapping("/me")
  public CommonResponse<Void> deleteMe(
      @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
      HttpServletResponse response
  ) {
    memberService.deleteMember(memberId);
    cookieUtil.expireCookie(response, "accessToken");
    cookieUtil.expireCookie(response, "refreshToken");
    return CommonResponse.ok();
  }
}
