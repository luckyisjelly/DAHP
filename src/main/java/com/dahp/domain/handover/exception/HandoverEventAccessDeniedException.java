package com.dahp.domain.handover.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class HandoverEventAccessDeniedException extends BusinessException {

    public HandoverEventAccessDeniedException() {
        super(ErrorCode.FORBIDDEN_RESOURCE);
    }
}
