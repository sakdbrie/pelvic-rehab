package com.pelvic.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 登录请求（密码/短信二选一，见后端实现逻辑.md 第 2.4 节） */
@Data
public class LoginRequest {

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号格式")
    private String phone;

    /** 密码登录 */
    private String password;

    /** 短信验证码登录 */
    private String smsCode;
}
