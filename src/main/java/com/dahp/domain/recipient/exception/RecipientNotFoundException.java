package com.dahp.domain.recipient.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class RecipientNotFoundException extends BusinessException {

    public RecipientNotFoundException() {
        super(ErrorCode.RECIPIENT_NOT_FOUND);
    }
}
