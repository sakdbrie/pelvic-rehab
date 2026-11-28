package com.pelvic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 盆底康复管理平台 · 网页后台后端入口
 * 接口前缀 /api/admin/v1/**（见 docs/后端实现逻辑.md）
 */
@SpringBootApplication
@ConfigurationPropertiesScan("com.pelvic")
public class PelvicAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(PelvicAdminApplication.class, args);
    }
}
