package com.pelvic.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 修改密码请求 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "请输入原密码")
    private String oldPassword;

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, message = "新密码长度不少于 6 位")
    private String newPassword;
}
