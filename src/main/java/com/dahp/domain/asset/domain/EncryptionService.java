package com.dahp.domain.asset.domain;

/**
 * 자산 내용 암호화/복호화 인터페이스.
 * MVP는 NoOp 구현체 사용. P2에서 AES-GCM 등 실 암호화 구현체로 교체.
 */
public interface EncryptionService {

    /**
     * 평문을 암호화. MVP NoOp에서는 입력 그대로 반환.
     */
    String encrypt(String plaintext);

    /**
     * 암호문을 복호화. MVP NoOp에서는 입력 그대로 반환.
     */
    String decrypt(String ciphertext);

    /**
     * 이 구현체가 실제로 암호화를 수행하는지 여부.
     * NoOp는 false, 실 구현체는 true.
     */
    boolean isEnabled();
}
