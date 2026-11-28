package com.pelvic.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 发送验证码请求 */
@Data
public class SmsCodeRequest {

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号格式")
    private String phone;

    /** REGISTER / LOGIN / BIND_PHONE / RESET_PASSWORD */
    @NotBlank(message = "缺少使用场景")
    private String scene;
}
