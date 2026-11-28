package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 神经损伤查体 */
@Data
@TableName("neurological_exam")
public class NeurologicalExam {

    @TableId(value = "查体ID", type = IdType.AUTO)
    private Long id;

    @TableField("检查ID")
    private Long examinationId;

    /** 正常 / 增高 / 降低 */
    @TableField("盆底张力")
    private String pelvicFloorTone;

    /** 正常 / 减弱 / 过敏 */
    @TableField("左侧感觉")
    private String leftSensation;

    /** 正常 / 减弱 / 过敏 */
    @TableField("右侧感觉")
    private String rightSensation;

    /** 导尿管（1=有） */
    @TableField("导尿管")
    private Boolean catheter;
}
