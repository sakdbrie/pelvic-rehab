package com.pelvic.system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 账号角色关联 */
@Data
@TableName("sys_user_role")
public class SysUserRole {

    @TableId(value = "账号角色关联ID", type = IdType.AUTO)
    private Long id;

    @TableField("账号ID")
    private Long accountId;

    @TableField("角色ID")
    private Long roleId;

    @TableField("授权人账号ID")
    private Long grantorAccountId;

    @TableField("授权时间")
    private LocalDateTime grantTime;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
