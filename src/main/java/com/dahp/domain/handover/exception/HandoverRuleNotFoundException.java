package com.dahp.domain.handover.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class HandoverRuleNotFoundException extends BusinessException {

    public HandoverRuleNotFoundException() {
        super(ErrorCode.RULE_NOT_FOUND);
    }
}
