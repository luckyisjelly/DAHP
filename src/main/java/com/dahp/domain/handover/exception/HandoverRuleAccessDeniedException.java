package com.dahp.domain.handover.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class HandoverRuleAccessDeniedException extends BusinessException {

    public HandoverRuleAccessDeniedException() {
        super(ErrorCode.FORBIDDEN_RESOURCE);
    }
}
