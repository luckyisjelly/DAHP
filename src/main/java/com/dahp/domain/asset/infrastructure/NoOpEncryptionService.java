package com.dahp.domain.asset.infrastructure;

import com.dahp.domain.asset.domain.EncryptionService;
import org.springframework.stereotype.Component;

/**
 * MVP용 NoOp 암호화 구현체.
 * 입력을 그대로 반환. P2에서 AesGcmEncryptionService 등으로 교체.
 */
@Component
public class NoOpEncryptionService implements EncryptionService {

    @Override
    public String encrypt(String plaintext) {
        return plaintext;
    }

    @Override
    public String decrypt(String ciphertext) {
        return ciphertext;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
