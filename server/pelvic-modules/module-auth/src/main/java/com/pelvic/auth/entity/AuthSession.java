package com.pelvic.auth.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 登录会话（客户端类型：WEB_ADMIN / MINI_DOCTOR / MINI_PATIENT） */
@Data
@TableName("auth_session")
public class AuthSession {

    @TableId(value = "会话ID", type = IdType.AUTO)
    private Long id;

    @TableField("账号ID")
    private Long accountId;

    @TableField("客户端类型")
    private String clientType;

    @TableField("AccessToken哈希")
    private String accessTokenHash;

    @TableField("RefreshToken哈希")
    private String refreshTokenHash;

    @TableField("过期时间")
    private LocalDateTime expireTime;

    @TableField("撤销时间")
    private LocalDateTime revokeTime;

    @TableField("最后活跃时间")
    private LocalDateTime lastActiveTime;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
