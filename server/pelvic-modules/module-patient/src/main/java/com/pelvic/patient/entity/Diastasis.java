package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 腹直肌分离评估 */
@Data
@TableName("diastasis")
public class Diastasis {

    @TableId(value = "评估ID", type = IdType.AUTO)
    private Long id;

    @TableField("检查ID")
    private Long examinationId;

    @TableField("静息肚脐指宽")
    private BigDecimal restingUmbilicus;

    @TableField("静息脐上指宽")
    private BigDecimal restingAbove;

    @TableField("静息脐下指宽")
    private BigDecimal restingBelow;

    @TableField("收缩肚脐指宽")
    private BigDecimal contractionUmbilicus;

    @TableField("收缩脐上指宽")
    private BigDecimal contractionAbove;

    @TableField("收缩脐下指宽")
    private BigDecimal contractionBelow;
}
