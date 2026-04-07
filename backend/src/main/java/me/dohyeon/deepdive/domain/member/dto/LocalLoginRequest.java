package me.dohyeon.deepdive.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로컬 로그인 요청")
public record LocalLoginRequest(
    @Schema(description = "이메일", example = "user@example.com") String email,
    @Schema(description = "비밀번호", example = "password1234!") String password
) {

}
