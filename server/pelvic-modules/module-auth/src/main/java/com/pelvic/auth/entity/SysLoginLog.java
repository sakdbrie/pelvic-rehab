package com.pelvic.auth.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 登录审计日志 */
@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId(value = "登录日志ID", type = IdType.AUTO)
    private Long id;

    @TableField("账号ID")
    private Long accountId;

    /** SMS_CODE / PASSWORD */
    @TableField("登录方式")
    private String loginType;

    @TableField("客户端类型")
    private String clientType;

    /** SUCCESS / FAILED */
    @TableField("登录结果")
    private String result;

    @TableField("失败原因")
    private String failReason;

    @TableField("IP地址")
    private String ip;

    @TableField("UserAgent")
    private String userAgent;

    @TableField(value = "登录时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime loginTime;
}
