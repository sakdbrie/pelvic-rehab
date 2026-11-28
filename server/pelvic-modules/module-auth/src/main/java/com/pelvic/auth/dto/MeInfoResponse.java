package com.pelvic.auth.dto;

import lombok.Data;

import java.util.List;

/** 当前登录用户信息（GET /auth/me，后端实现逻辑.md 第 26 节） */
@Data
public class MeInfoResponse {

    private Long accountId;
    private String accountNo;
    private String nickname;
    /** 展示姓名（医生档案姓名，无档案时为昵称） */
    private String name;
    /** 业务身份编号：医生 D001 格式 / 患者编号 */
    private String userId;
    private String roleCode;
    private String roleName;
    /** 手机号（脱敏） */
    private String phone;
    private String deptName;
    private String title;
    private List<String> permissions;
}
