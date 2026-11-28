package com.pelvic.common.core.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** 哈希工具：Token/验证码一律只存 SHA-256 哈希 */
public final class HashUtil {

    private HashUtil() {
    }

    public static String sha256Hex(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 计算失败", e);
        }
    }
}
