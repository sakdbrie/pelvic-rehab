package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 盆底视诊 */
@Data
@TableName("pelvic_inspection")
public class PelvicInspection {

    @TableId(value = "视诊ID", type = IdType.AUTO)
    private Long id;

    @TableField("检查ID")
    private Long examinationId;

    @TableField("外阴情况")
    private String vulva;

    @TableField("阴道裂口")
    private String vaginalTear;

    @TableField("尿道口")
    private String urethralMeatus;

    @TableField("阴蒂")
    private String clitoris;

    /** 阴唇对称（1=是） */
    @TableField("阴唇对称")
    private Boolean labiaSymmetry;

    /** 分泌物情况（1=有） */
    @TableField("分泌物情况")
    private Boolean discharge;

    @TableField("分泌物颜色")
    private String dischargeColor;

    @TableField("分泌物气味")
    private String dischargeSmell;

    @TableField("分泌物质地")
    private String dischargeTexture;

    @TableField("分泌物量")
    private BigDecimal dischargeAmount;

    @TableField("阴毛情况")
    private String pubicHair;

    @TableField("痔疮情况")
    private String hemorrhoids;

    @TableField("Valsalva表现")
    private String valsalva;
}
