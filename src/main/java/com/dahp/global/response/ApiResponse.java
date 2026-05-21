package com.dahp.global.response;

import com.dahp.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        ErrorInfo error,
        OffsetDateTime timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data, null, OffsetDateTime.now());
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null, null, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null, OffsetDateTime.now());
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, message, null, null, OffsetDateTime.now());
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, null, ErrorInfo.from(errorCode), OffsetDateTime.now());
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, null, ErrorInfo.of(errorCode, message), OffsetDateTime.now());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorInfo(String code, String message, String hint) {
        public static ErrorInfo from(ErrorCode errorCode) {
            return new ErrorInfo(errorCode.name(), errorCode.getMessage(), errorCode.getHint());
        }

        public static ErrorInfo of(ErrorCode errorCode, String message) {
            return new ErrorInfo(errorCode.name(), message, errorCode.getHint());
        }
    }
}
