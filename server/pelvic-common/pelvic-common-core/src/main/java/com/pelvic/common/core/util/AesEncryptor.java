package com.pelvic.common.core.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES/CBC/PKCS5 加解密：手机号密文存库。
 * 构造参数为 Base64 编码的 16/24/32 字节密钥（配置 pelvic.encrypt.aes-key）。
 */
public class AesEncryptor {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final byte[] key;

    public AesEncryptor(String base64Key) {
        this.key = Base64.getDecoder().decode(base64Key);
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("AES 密钥长度必须为 16/24/32 字节（Base64 编码）");
        }
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[16];
            RANDOM.nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("AES 加密失败", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }
        try {
            byte[] in = Base64.getDecoder().decode(cipherText);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(in, 0, 16));
            return new String(cipher.doFinal(in, 16, in.length - 16), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("AES 解密失败", e);
        }
    }
}
