package com.dahp.domain.handover.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class AccessTokenExpiredException extends BusinessException {

    public AccessTokenExpiredException() {
        super(ErrorCode.ACCESS_TOKEN_EXPIRED);
    }
}
