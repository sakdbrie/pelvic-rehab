package com.pelvic.system.service;

import com.pelvic.common.security.AuthUserLoader;
import com.pelvic.common.security.model.UserInfo;
import com.pelvic.system.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 账号角色权限加载（RBAC：sys_user_role / sys_role_permission / sys_permission） */
@Service
public class UserInfoLoader implements AuthUserLoader {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public UserInfo load(Long accountId) {
        UserInfo info = new UserInfo();
        info.setRoles(sysUserRoleMapper.selectRolesByAccountId(accountId));
        info.setPerms(sysUserRoleMapper.selectPermCodesByAccountId(accountId));
        return info;
    }
}
