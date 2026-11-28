package com.pelvic.auth.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 短信验证码（验证码只存哈希；使用时间 NULL=未使用） */
@Data
@TableName("auth_sms_code")
public class AuthSmsCode {

    @TableId(value = "验证码ID", type = IdType.AUTO)
    private Long id;

    @TableField("手机号密文")
    private String phoneCipher;

    @TableField("手机号HMAC")
    private String phoneHmac;

    /** REGISTER / LOGIN / BIND_PHONE / RESET_PASSWORD */
    @TableField("使用场景")
    private String scene;

    @TableField("验证码哈希")
    private String codeHash;

    @TableField("过期时间")
    private LocalDateTime expireTime;

    @TableField("使用时间")
    private LocalDateTime usedTime;

    @TableField("验证失败次数")
    private Integer failCount;

    @TableField("发送IP")
    private String sendIp;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
