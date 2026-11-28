package com.pelvic.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** 安全配置：pelvic.security.* */
@Data
@Component
@ConfigurationProperties(prefix = "pelvic.security")
public class SecurityProperties {

    /** 免登录路径（前缀匹配） */
    private List<String> whitelist = new ArrayList<>();
    /** accessToken 有效期（秒） */
    private long tokenTtlSeconds = 7200;
}
