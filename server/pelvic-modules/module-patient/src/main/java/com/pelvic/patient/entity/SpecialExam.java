package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 特殊检查（+/-） */
@Data
@TableName("special_exam")
public class SpecialExam {

    @TableId(value = "特殊检查ID", type = IdType.AUTO)
    private Long id;

    @TableField("检查ID")
    private Long examinationId;

    @TableField("泌尿生殖测试")
    private String urogenitalTest;

    @TableField("疤痕试验")
    private String scarTest;

    @TableField("4字试验左")
    private String faberLeft;

    @TableField("4字试验右")
    private String faberRight;

    @TableField("骨盆分离试验")
    private String pelvicSeparation;

    @TableField("骨盆挤压试验")
    private String pelvicCompression;

    @TableField("骶骨下压试验")
    private String sacralCompression;

    @TableField("股骨冲击试验")
    private String femoralImpaction;

    @TableField("直腿抬高试验")
    private String straightLegRaise;

    @TableField("直腿抬高加强试验")
    private String straightLegRaiseReinforced;

    @TableField("广泛性触诊试验")
    private String palpationTest;

    @TableField("其他")
    private String other;
}
