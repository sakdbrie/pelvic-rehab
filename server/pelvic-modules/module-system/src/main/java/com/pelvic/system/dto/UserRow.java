package com.pelvic.system.dto;

import lombok.Data;

/** 用户列表查询行（phoneCipher 由服务层解密脱敏后转为 UserRowResp） */
@Data
public class UserRow {

    /** 患者编号 或 D001 格式医生编号 */
    private String id;
    private String name;
    /** patient / doctor */
    private String role;
    private String phoneCipher;
}
