package com.dahp.domain.handover.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class HandoverEventNotFoundException extends BusinessException {

    public HandoverEventNotFoundException() {
        super(ErrorCode.EVENT_NOT_FOUND);
    }
}
