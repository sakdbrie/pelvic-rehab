package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 疼痛评估（9 个部位 左/右/中 VAS） */
@Data
@TableName("pain_assessment")
public class PainAssessment {

    @TableId(value = "疼痛评估ID", type = IdType.AUTO)
    private Long id;

    @TableField("检查ID")
    private Long examinationId;

    @TableField("腰背痛左VAS")
    private Integer backPainLeftVas;

    @TableField("腰背痛右VAS")
    private Integer backPainRightVas;

    @TableField("腰背痛中VAS")
    private Integer backPainCenterVas;

    @TableField("骶髂关节痛左VAS")
    private Integer siJointPainLeftVas;

    @TableField("骶髂关节痛右VAS")
    private Integer siJointPainRightVas;

    @TableField("骶髂关节痛中VAS")
    private Integer siJointPainCenterVas;

    @TableField("耻骨痛VAS")
    private Integer pubicPainVas;

    @TableField("尾骨痛左VAS")
    private Integer coccyxPainLeftVas;

    @TableField("尾骨痛右VAS")
    private Integer coccyxPainRightVas;

    @TableField("尾骨痛中VAS")
    private Integer coccyxPainCenterVas;

    @TableField("坐骨神经痛左VAS")
    private Integer sciaticPainLeftVas;

    @TableField("坐骨神经痛右VAS")
    private Integer sciaticPainRightVas;

    @TableField("腹股沟痛左VAS")
    private Integer groinPainLeftVas;

    @TableField("腹股沟痛右VAS")
    private Integer groinPainRightVas;

    @TableField("颈痛左VAS")
    private Integer neckPainLeftVas;

    @TableField("颈痛右VAS")
    private Integer neckPainRightVas;

    @TableField("上肢痛左VAS")
    private Integer armPainLeftVas;

    @TableField("上肢痛右VAS")
    private Integer armPainRightVas;

    @TableField("下肢痛左VAS")
    private Integer legPainLeftVas;

    @TableField("下肢痛右VAS")
    private Integer legPainRightVas;
}
