package me.dohyeon.deepdive.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import me.dohyeon.deepdive.domain.member.entity.Member;

@Schema(description = "회원 응답")
public record MemberResponse(
    @Schema(description = "회원 ID", example = "1") Long id,
    @Schema(description = "이메일", example = "user@example.com") String email,
    @Schema(description = "닉네임", example = "도현") String nickname
) {

  public static MemberResponse from(Member member) {
    return new MemberResponse(member.getId(), member.getEmail(), member.getNickname());
  }
}
