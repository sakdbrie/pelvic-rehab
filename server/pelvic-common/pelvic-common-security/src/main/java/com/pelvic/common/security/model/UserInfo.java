package com.pelvic.common.security.model;

import lombok.Data;

import java.util.List;

/** 账号的角色与权限信息（由 AuthUserLoader 加载） */
@Data
public class UserInfo {

    private String nickname;
    private List<RoleInfo> roles = List.of();
    private List<String> perms = List.of();

    @Data
    public static class RoleInfo {
        private String code;
        private String name;
    }
}
