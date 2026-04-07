package me.dohyeon.deepdive.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로컬 회원가입 요청")
public record LocalSignupRequest(
    @Schema(description = "이메일 (로그인 아이디)", example = "user@example.com") String email,
    @Schema(description = "비밀번호", example = "password1234!") String password,
    @Schema(description = "닉네임 (최대 20자)", example = "도현") String nickname
) {

}
