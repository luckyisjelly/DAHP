package com.dahp.domain.handover.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class AccessTokenCancelledException extends BusinessException {

    public AccessTokenCancelledException() {
        super(ErrorCode.ACCESS_TOKEN_CANCELLED);
    }
}
