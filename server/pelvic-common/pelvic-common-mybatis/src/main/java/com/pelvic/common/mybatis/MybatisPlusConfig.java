package com.pelvic.common.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：分页插件；Mapper 扫描。
 * 注意：扫描范围必须限定在各模块的 *.mapper 包，避免把 AuthService/AuthUserLoader/SmsSender
 * 等普通接口误注册为 Mapper 代理（会导致 Invalid bound statement）。
 */
@Configuration
@MapperScan(basePackages = {
        "com.pelvic.auth.mapper",
        "com.pelvic.system.mapper",
        "com.pelvic.patient.mapper",
        "com.pelvic.doctor.mapper",
        "com.pelvic.report.mapper"
})
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
