package me.dohyeon.deepdive.domain.member.controller;

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

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberProfileController {

  private final MemberService memberService;
  private final CookieUtil cookieUtil;

  @GetMapping("/me")
  public CommonResponse<MemberResponse> getMe(
      @AuthenticationPrincipal Long memberId
  ) {
    Member member = memberService.getMember(memberId);
    MemberResponse memberResponse = MemberResponse.from(member);
    return CommonResponse.ok(memberResponse);
  }

  @PatchMapping("/me")
  public CommonResponse<MemberResponse> updateMe(
      @AuthenticationPrincipal Long memberId,
      @RequestBody UpdateNicknameRequest request
  ) {
    Member member = memberService.updateNickname(memberId, request.nickname());
    MemberResponse memberResponse = MemberResponse.from(member);
    return CommonResponse.ok(memberResponse);
  }

  @DeleteMapping("/me")
  public CommonResponse<Void> deleteMe(
      @AuthenticationPrincipal Long memberId,
      HttpServletResponse response
  ) {
    memberService.deleteMember(memberId);
    cookieUtil.expireCookie(response, "accessToken");
    cookieUtil.expireCookie(response, "refreshToken");
    return CommonResponse.ok();
  }
}
