package com.dahp.domain.handover.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class AccessTokenUsedException extends BusinessException {

    public AccessTokenUsedException() {
        super(ErrorCode.ACCESS_TOKEN_USED);
    }
}
