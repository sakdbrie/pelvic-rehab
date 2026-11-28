package com.pelvic.common.core.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * HMAC-SHA256 签名：手机号HMAC 存库/查询，明文手机号不落库。
 * 密钥来自配置 pelvic.encrypt.hmac-key（Bean 在 pelvic-common-security 中装配）。
 */
public class HmacSigner {

    private final byte[] key;

    public HmacSigner(String key) {
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }

    public String sign(String input) {
        if (input == null) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] digest = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }
}
