package com.pelvic.common.security;

import com.pelvic.common.core.util.AesEncryptor;
import com.pelvic.common.core.util.HmacSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 加密组件装配（密钥来自 pelvic.encrypt.* 配置） */
@Configuration
public class CryptoConfig {

    @Bean
    public AesEncryptor aesEncryptor(@Value("${pelvic.encrypt.aes-key}") String base64Key) {
        return new AesEncryptor(base64Key);
    }

    @Bean
    public HmacSigner hmacSigner(@Value("${pelvic.encrypt.hmac-key}") String key) {
        return new HmacSigner(key);
    }
}
