package com.dahp.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 검증에 실패했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다."),

    // Auth (Phase 2 사용)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // Asset (Phase 3 사용)
    ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "자산을 찾을 수 없습니다."),

    // Recipient (Sprint 3 사용)
    RECIPIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수령인을 찾을 수 없습니다."),

    // HandoverRule / Event (Sprint 3 사용)
    RULE_NOT_FOUND(HttpStatus.NOT_FOUND, "인계 규칙을 찾을 수 없습니다."),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "인계 이벤트를 찾을 수 없습니다."),
    INVALID_STATE_TRANSITION(HttpStatus.CONFLICT, "허용되지 않은 상태 전이입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.GONE, "접근 토큰이 만료되었습니다."),
    ACCESS_TOKEN_USED(HttpStatus.GONE, "이미 사용된 접근 토큰입니다."),

    // 공통 권한
    FORBIDDEN_RESOURCE(HttpStatus.FORBIDDEN, "해당 리소스에 접근할 권한이 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
