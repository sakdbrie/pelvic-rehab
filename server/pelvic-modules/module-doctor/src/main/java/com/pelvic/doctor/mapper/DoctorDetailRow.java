package com.pelvic.doctor.mapper;

import lombok.Data;

/** 医生详情联表查询行（doctor + department + account；phoneCipher 由服务层解密脱敏） */
@Data
public class DoctorDetailRow {

    private Long id;
    private String name;
    private String gender;
    private String title;
    private Long deptId;
    private String deptName;
    private String auditStatus;
    private String phoneCipher;
    private String auditReason;
}
