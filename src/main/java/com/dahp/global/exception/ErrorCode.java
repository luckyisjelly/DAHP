package com.dahp.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 검증에 실패했습니다.", "요청 본문의 필수 필드와 형식을 다시 확인해주세요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.", "잠시 후 다시 시도해주세요. 계속되면 관리자에게 문의해주세요."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다.", null),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다.", "Swagger UI(/swagger-ui/index.html)에서 사용 가능한 엔드포인트를 확인하세요."),

    // Auth (Phase 2 사용)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.", "다른 이메일을 사용하거나 기존 계정으로 로그인해주세요."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.", "비밀번호를 다시 확인해주세요. (보안상 어느 쪽이 틀린지는 알려드리지 않습니다.)"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", "로그인을 다시 시도해 새 토큰을 발급받으세요."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.", "POST /api/auth/refresh 로 새 access 토큰을 발급받으세요."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", null),

    // Asset (Phase 3 사용)
    ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "자산을 찾을 수 없습니다.", "이미 삭제되었거나 본인 소유가 아닌 자산일 수 있습니다."),

    // Recipient (Sprint 3 사용)
    RECIPIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "수령인을 찾을 수 없습니다.", null),

    // HandoverRule / Event (Sprint 3 사용)
    RULE_NOT_FOUND(HttpStatus.NOT_FOUND, "인계 규칙을 찾을 수 없습니다.", null),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "인계 이벤트를 찾을 수 없습니다.", null),
    INVALID_STATE_TRANSITION(HttpStatus.CONFLICT, "허용되지 않은 상태 전이입니다.", "현재 상태에서 가능한 액션을 확인해주세요."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.GONE, "접근 토큰이 만료되었습니다.", "자산 소유자에게 새 토큰을 요청해주세요."),
    ACCESS_TOKEN_USED(HttpStatus.GONE, "이미 사용된 접근 토큰입니다.", "토큰은 1회만 사용 가능합니다. 자산 소유자에게 새 토큰을 요청해주세요."),

    // 공통 권한
    FORBIDDEN_RESOURCE(HttpStatus.FORBIDDEN, "해당 리소스에 접근할 권한이 없습니다.", "본인이 소유한 리소스만 조회/수정할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String message;
    private final String hint;

    ErrorCode(HttpStatus status, String message, String hint) {
        this.status = status;
        this.message = message;
        this.hint = hint;
    }
}
