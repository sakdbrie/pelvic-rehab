package com.pelvic.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 添加医生请求（后端实现逻辑.md 第 7.4 节） */
@Data
public class DoctorCreateRequest {

    @NotBlank(message = "请输入姓名")
    private String name;

    @NotBlank(message = "请选择性别")
    private String gender;

    @NotNull(message = "请选择科室")
    private Long deptId;

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号格式")
    private String phone;
}
