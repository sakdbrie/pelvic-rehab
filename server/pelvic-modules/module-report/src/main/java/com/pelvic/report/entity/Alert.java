package com.pelvic.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 异常预警（无创建/更新时间列） */
@Data
@TableName("alert")
public class Alert {

    @TableId(value = "预警ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    @TableField("预警规则ID")
    private Long ruleId;

    @TableField("触发排尿记录ID")
    private Long triggerUrinationRecordId;

    @TableField("触发排便记录ID")
    private Long triggerDefecationRecordId;

    @TableField("预警时间")
    private LocalDateTime alertTime;

    @TableField("预警内容")
    private String content;

    /** 待处理 / 处理中 / 已处理 / 已忽略 */
    @TableField("处理状态")
    private String handleStatus;

    @TableField("处理医生ID")
    private Long handleDoctorId;

    @TableField("处理时间")
    private LocalDateTime handleTime;

    @TableField("处理备注")
    private String handleRemark;
}
