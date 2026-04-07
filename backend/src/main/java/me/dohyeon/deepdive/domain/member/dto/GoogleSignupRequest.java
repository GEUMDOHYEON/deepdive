package me.dohyeon.deepdive.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구글 회원가입 완료 요청")
public record GoogleSignupRequest(
    @Schema(description = "닉네임 (최대 20자)", example = "도현") String nickname
) {

}
