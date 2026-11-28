package com.pelvic.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.common.security.model.UserInfo;
import com.pelvic.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("SELECT r.角色编码 AS code, r.角色名称 AS name FROM sys_user_role ur "
            + "JOIN sys_role r ON r.角色ID = ur.角色ID "
            + "WHERE ur.账号ID = #{accountId} AND r.角色状态 = '启用' ORDER BY r.角色ID")
    List<UserInfo.RoleInfo> selectRolesByAccountId(Long accountId);

    @Select("SELECT DISTINCT p.权限编码 FROM sys_user_role ur "
            + "JOIN sys_role_permission rp ON rp.角色ID = ur.角色ID "
            + "JOIN sys_permission p ON p.权限ID = rp.权限ID "
            + "WHERE ur.账号ID = #{accountId} AND p.权限编码 IS NOT NULL")
    List<String> selectPermCodesByAccountId(Long accountId);
}
