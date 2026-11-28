package com.pelvic.common.security;

import com.pelvic.common.security.model.UserInfo;

/**
 * 账号角色权限加载器。实现位于 module-system（UserInfoLoader）：
 * 查 sys_user_role / sys_role_permission / sys_permission。
 */
public interface AuthUserLoader {

    UserInfo load(Long accountId);
}
