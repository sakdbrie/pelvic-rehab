package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 临床补充记录 */
@Data
@TableName("clinical_note")
public class ClinicalNote {

    @TableId(value = "临床记录ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    @TableField("检查ID")
    private Long examinationId;

    @TableField("医生ID")
    private Long doctorId;

    /** 外院资料 / 阶段评估 / 特殊情况 / 其他 */
    @TableField("记录类型")
    private String noteType;

    @TableField("记录内容")
    private String content;

    /** 医务人员 */
    @TableField("可见范围")
    private String visibility;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
