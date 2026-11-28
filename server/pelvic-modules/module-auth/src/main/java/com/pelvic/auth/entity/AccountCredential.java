package com.pelvic.auth.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 网页密码凭证（登录名即手机号；一个账号最多一条） */
@Data
@TableName("account_credential")
public class AccountCredential {

    @TableId(value = "凭证ID", type = IdType.AUTO)
    private Long id;

    @TableField("账号ID")
    private Long accountId;

    @TableField("密码哈希")
    private String passwordHash;

    @TableField("密码更新时间")
    private LocalDateTime passwordUpdateTime;

    @TableField("连续失败次数")
    private Integer failCount;

    @TableField("锁定截止时间")
    private LocalDateTime lockedUntil;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
