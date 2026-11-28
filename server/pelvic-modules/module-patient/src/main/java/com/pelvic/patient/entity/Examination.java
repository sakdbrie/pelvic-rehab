package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 临床检查主信息 */
@Data
@TableName("examination")
public class Examination {

    @TableId(value = "检查ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    @TableField("录入医生ID")
    private Long doctorId;

    @TableField("最后修改医生ID")
    private Long lastModifyDoctorId;

    @TableField("检查时间")
    private LocalDateTime examTime;

    @TableField("主诉")
    private String chief;

    /** 正常 / 增大 / 降低 */
    @TableField("腹部张力")
    private String abdominalTension;

    @TableField("诊断")
    private String diagnosis;

    @TableField("药物治疗")
    private String medication;

    @TableField("其他补充")
    private String otherSupplement;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
