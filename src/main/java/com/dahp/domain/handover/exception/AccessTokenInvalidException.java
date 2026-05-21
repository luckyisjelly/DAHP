package com.dahp.domain.handover.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class AccessTokenInvalidException extends BusinessException {

    public AccessTokenInvalidException() {
        super(ErrorCode.INVALID_ACCESS_TOKEN);
    }
}
