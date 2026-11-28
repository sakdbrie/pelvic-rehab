package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 妊娠记录（一行代表一次妊娠） */
@Data
@TableName("pregnancy")
public class Pregnancy {

    @TableId(value = "妊娠记录ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    @TableField("妊娠序号")
    private Integer pregnancyNo;

    @TableField("妊娠结局")
    private String outcome;

    @TableField("孕前体重kg")
    private BigDecimal preWeightKg;

    @TableField("产前体重kg")
    private BigDecimal prenatalWeightKg;

    @TableField("孕期增重kg")
    private BigDecimal weightGainKg;

    /** 左侧 / 右侧 / 仰卧 */
    @TableField("睡觉体位")
    private String sleepPosition;

    /** 尿失禁（1=有） */
    @TableField("尿失禁")
    private Boolean incontinence;

    /** 正常 / 失禁 / 便秘 */
    @TableField("大便情况")
    private String stool;

    /** 高 / 低 / 正常 */
    @TableField("血压情况")
    private String bloodPressure;

    /** 高 / 低 / 正常 */
    @TableField("血糖情况")
    private String bloodSugar;

    /** 甲亢 / 甲减 / 正常 */
    @TableField("甲状腺功能")
    private String thyroidFunction;

    @TableField("腰背痛VAS")
    private Integer backPainVas;

    @TableField("耻骨疼痛VAS")
    private Integer pubicPainVas;

    @TableField("下肢疼痛VAS")
    private Integer lowerLimbPainVas;

    @TableField("妊娠反应等级")
    private String reactionLevel;

    /** 会阴瘙痒（1=有） */
    @TableField("会阴瘙痒")
    private Boolean perinealItching;

    @TableField("饮食情况")
    private String diet;

    /** 孕期性生活（1=有） */
    @TableField("性生活")
    private Boolean sexLife;

    @TableField("分娩日期")
    private LocalDate deliveryDate;

    /** 顺产 / 剖宫产 */
    @TableField("分娩方式")
    private String deliveryMethod;

    /** 会阴侧切 / 会阴撕裂 / 无损伤 */
    @TableField("会阴情况")
    private String perineumCondition;

    @TableField("无痛分娩")
    private Boolean painlessDelivery;

    @TableField("头位正常分娩")
    private Boolean cephalicDelivery;

    /** 无 / 产钳 / 胎头吸引 */
    @TableField("助产方式")
    private String deliveryAssist;

    @TableField("胎儿出生体重kg")
    private BigDecimal babyBirthWeightKg;

    @TableField("备注")
    private String remark;

    /** 逻辑删除（1=已删除） */
    @TableField("逻辑删除")
    private Integer deleted;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
