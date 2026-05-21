package com.dahp.domain.handover.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class InvalidStateTransitionException extends BusinessException {

    public InvalidStateTransitionException(Enum<?> current, String target) {
        super(ErrorCode.INVALID_STATE_TRANSITION,
                String.format("현재 상태 %s에서 %s(으)로 전이할 수 없습니다.", current, target));
    }
}
