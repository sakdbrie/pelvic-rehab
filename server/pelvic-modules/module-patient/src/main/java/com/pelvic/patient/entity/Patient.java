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

/** 患者档案 */
@Data
@TableName("patient")
public class Patient {

    @TableId(value = "患者ID", type = IdType.AUTO)
    private Long id;

    @TableField("账号ID")
    private Long accountId;

    @TableField("患者编号")
    private String patientNo;

    @TableField("住院号")
    private String hospitalNo;

    @TableField("姓名")
    private String name;

    /** 男 / 女 */
    @TableField("性别")
    private String gender;

    @TableField("出生年月")
    private LocalDate birthDate;

    @TableField("身高cm")
    private BigDecimal heightCm;

    @TableField("体重kg")
    private BigDecimal weightKg;

    @TableField("婚姻状况")
    private String marriage;

    @TableField("教育程度")
    private String education;

    @TableField("从事职业")
    private String job;

    @TableField("备注")
    private String remark;

    /** 逻辑删除（1=已删除） */
    @TableField("是否删除")
    private Integer deleted;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
