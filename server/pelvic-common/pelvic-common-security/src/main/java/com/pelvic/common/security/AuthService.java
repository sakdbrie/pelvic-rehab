package com.pelvic.common.security;

/**
 * 会话解析服务：按 accessToken 还原登录上下文。
 * 实现位于 module-auth（AuthServiceImpl）：token 哈希查 auth_session → account → 角色权限。
 */
public interface AuthService {

    /** 解析 accessToken；无效/过期/已撤销返回 null */
    AuthContext parse(String accessToken);
}
