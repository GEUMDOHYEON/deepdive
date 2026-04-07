package me.dohyeon.deepdive.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Member
    MEMBER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "M001", "이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "존재하지 않는 회원입니다."),
    MEMBER_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "M003", "비밀번호가 일치하지 않습니다."),

    // Auth
    SIGNUP_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 회원가입 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A002", "리프레시 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 리프레시 토큰입니다."),

    // Interview
    INTERVIEW_CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "I001", "유효하지 않은 면접 카테고리입니다."),
    INTERVIEW_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "I002", "면접 세션을 찾을 수 없습니다."),
    INTERVIEW_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "I003", "면접 질문을 찾을 수 없습니다."),
    INTERVIEW_SESSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "I004", "이미 완료된 면접 세션입니다."),
    AI_EVALUATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "I005", "AI 답변 평가 중 오류가 발생했습니다."),
    INTERVIEW_SESSION_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "I006", "아직 완료되지 않은 면접 세션입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
