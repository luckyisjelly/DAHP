package com.dahp.domain.recipient.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class RecipientAccessDeniedException extends BusinessException {

    public RecipientAccessDeniedException() {
        super(ErrorCode.FORBIDDEN_RESOURCE);
    }
}
