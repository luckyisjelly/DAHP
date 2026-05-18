package com.dahp.global.response;

import com.dahp.global.exception.ErrorCode;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorInfo error,
        OffsetDateTime timestamp
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, OffsetDateTime.now());
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null, OffsetDateTime.now());
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ErrorInfo.from(errorCode), OffsetDateTime.now());
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, new ErrorInfo(errorCode.name(), message), OffsetDateTime.now());
    }

    public record ErrorInfo(String code, String message) {
        public static ErrorInfo from(ErrorCode errorCode) {
            return new ErrorInfo(errorCode.name(), errorCode.getMessage());
        }
    }
}
