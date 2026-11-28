package com.pelvic.common.security;

import com.pelvic.common.core.util.HashUtil;

import java.security.SecureRandom;
import java.util.Base64;

/** Token 工具：随机不透明 token（存哈希），前端以 Bearer 携带 */
public final class TokenUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenUtil() {
    }

    /** 生成 32 字节随机 token（Base64URL，无填充） */
    public static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** token 入库哈希 */
    public static String hash(String token) {
        return HashUtil.sha256Hex(token);
    }
}
