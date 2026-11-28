package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 产后情况评估 */
@Data
@TableName("postpartum")
public class Postpartum {

    @TableId(value = "评估ID", type = IdType.AUTO)
    private Long id;

    @TableField("检查ID")
    private Long examinationId;

    /** 无 / 压力性 / 急迫性 / 充溢性 / 混合性 */
    @TableField("小便情况")
    private String urine;

    /** 正常 / 失禁 / 便秘 */
    @TableField("大便情况")
    private String stool;

    @TableField("盆腔会阴痛VAS")
    private Integer pelvicPerinealPainVas;

    @TableField("脏器脱垂")
    private String organProlapse;

    /** 瘢痕情况（1=有） */
    @TableField("瘢痕情况")
    private Boolean scar;

    @TableField("瘢痕质地")
    private String scarTexture;

    @TableField("瘢痕颜色")
    private String scarColor;

    @TableField("瘢痕时间")
    private LocalDate scarDate;

    @TableField("瘢痕长度cm")
    private BigDecimal scarLengthCm;

    /** 尿道不稳定性（1=有） */
    @TableField("尿道不稳定性")
    private Boolean urethralInstability;

    @TableField("其他")
    private String other;
}
