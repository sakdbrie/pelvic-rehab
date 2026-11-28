package com.pelvic.system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 操作审计日志（添加/移除医生、导出报表等写审计） */
@Data
@TableName("sys_operate_log")
public class SysOperateLog {

    @TableId(value = "日志ID", type = IdType.AUTO)
    private Long id;

    @TableField("操作账号ID")
    private Long accountId;

    @TableField("关联患者ID")
    private Long patientId;

    @TableField("业务模块")
    private String module;

    @TableField("资源类型")
    private String resourceType;

    @TableField("资源ID")
    private String resourceId;

    /** VIEW / CREATE / UPDATE / DELETE / EXPORT / LOGIN / OTHER */
    @TableField("操作类型")
    private String action;

    /** SUCCESS / DENIED / FAILED */
    @TableField("操作结果")
    private String result;

    @TableField("操作原因")
    private String reason;

    @TableField("请求方法")
    private String method;

    @TableField("请求地址")
    private String uri;

    @TableField("请求参数")
    private String params;

    @TableField("耗时毫秒")
    private Integer costMs;

    @TableField("IP地址")
    private String ip;

    @TableField("userAgent")
    private String userAgent;

    @TableField(value = "操作时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime operateTime;
}
