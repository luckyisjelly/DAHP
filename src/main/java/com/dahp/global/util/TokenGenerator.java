package com.dahp.global.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 수령인 접근용 secure random 토큰 생성.
 * 32바이트 random → base64url 인코딩 (≈43자).
 */
public final class TokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private TokenGenerator() {
    }

    public static String generate() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        RANDOM.nextBytes(bytes);
        return BASE64_URL_ENCODER.encodeToString(bytes);
    }
}
