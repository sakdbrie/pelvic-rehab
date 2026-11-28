package com.pelvic.auth.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 统一账号主体（手机号为唯一登录身份，密文/HMAC 存于本表） */
@Data
@TableName("account")
public class Account {

    @TableId(value = "账号ID", type = IdType.AUTO)
    private Long id;

    @TableField("手机号密文")
    private String phoneCipher;

    @TableField("手机号HMAC")
    private String phoneHmac;

    @TableField("账号编号")
    private String accountNo;

    @TableField("昵称")
    private String nickname;

    /** PENDING / NORMAL / DISABLED / CANCELLED */
    @TableField("账号状态")
    private String status;

    /** MINI_PROGRAM / WEB / ADMIN */
    @TableField("注册来源")
    private String registerSource;

    @TableField("最后登录时间")
    private LocalDateTime lastLoginTime;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
