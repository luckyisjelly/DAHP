package com.dahp.domain.asset.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class AssetAccessDeniedException extends BusinessException {

    public AssetAccessDeniedException() {
        super(ErrorCode.FORBIDDEN_RESOURCE);
    }
}
