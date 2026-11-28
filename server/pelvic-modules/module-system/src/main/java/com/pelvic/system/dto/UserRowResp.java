package com.pelvic.system.dto;

import lombok.Data;

/** 用户列表响应行（与前端 UserRow {id,name,role,phone} 对齐，手机号已脱敏） */
@Data
public class UserRowResp {

    private String id;
    private String name;
    private String role;
    private String phone;
}
