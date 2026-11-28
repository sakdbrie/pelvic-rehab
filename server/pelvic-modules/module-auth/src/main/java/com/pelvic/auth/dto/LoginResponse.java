package com.pelvic.auth.dto;

import lombok.Data;

import java.util.List;

/** 登录成功响应（结构见后端实现逻辑.md 第 2.5 节“响应数据”） */
@Data
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private AccountInfo account;
    private UserInfo user;

    @Data
    public static class AccountInfo {
        private Long accountId;
        private String nickname;

        public AccountInfo(Long accountId, String nickname) {
            this.accountId = accountId;
            this.nickname = nickname;
        }
    }

    @Data
    public static class UserInfo {
        private String userId;
        private String roleCode;
        private String roleName;
        private List<String> permissions;
    }
}
