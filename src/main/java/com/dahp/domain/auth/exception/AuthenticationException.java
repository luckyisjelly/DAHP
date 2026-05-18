package com.dahp.domain.auth.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class AuthenticationException extends BusinessException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static AuthenticationException loginFailed() {
        return new AuthenticationException(ErrorCode.LOGIN_FAILED);
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException(ErrorCode.INVALID_TOKEN);
    }

    public static AuthenticationException tokenExpired() {
        return new AuthenticationException(ErrorCode.TOKEN_EXPIRED);
    }
}
