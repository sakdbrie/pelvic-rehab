package com.pelvic.report.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 量表填写记录 */
@Data
@TableName("questionnaire_submission")
public class QuestionnaireSubmission {

    @TableId(value = "填写记录ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    @TableField("量表ID")
    private Long questionnaireId;

    @TableField("填写时间")
    private LocalDateTime fillTime;

    /** 未完成 / 已提交 */
    @TableField("填写状态")
    private String fillStatus;

    @TableField("总分")
    private BigDecimal totalScore;

    /** JSON 维度得分快照 */
    @TableField("维度得分快照")
    private String dimensionScores;

    @TableField("评估结果")
    private String result;

    @TableField("提交时间")
    private LocalDateTime submitTime;

    @TableField("医生点评")
    private String doctorComment;

    @TableField("点评医生ID")
    private Long commentDoctorId;

    @TableField("点评时间")
    private LocalDateTime commentTime;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
