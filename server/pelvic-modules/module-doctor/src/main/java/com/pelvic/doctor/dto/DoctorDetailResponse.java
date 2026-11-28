package com.pelvic.doctor.dto;

import lombok.Data;

/** 医生详情响应（编号 D + 3 位数字格式，手机号已脱敏） */
@Data
public class DoctorDetailResponse {

    /** 医生编号（如 D001） */
    private String id;
    private String name;
    private String gender;
    private String title;
    private Long deptId;
    private String deptName;
    /** 手机号（脱敏） */
    private String phone;
    private String auditStatus;
    private String auditReason;
}
