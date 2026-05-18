package com.dahp.domain.asset.exception;

import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;

public class AssetNotFoundException extends BusinessException {

    public AssetNotFoundException() {
        super(ErrorCode.ASSET_NOT_FOUND);
    }
}
