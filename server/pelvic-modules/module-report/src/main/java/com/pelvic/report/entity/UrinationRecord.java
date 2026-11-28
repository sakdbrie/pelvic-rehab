package com.pelvic.report.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 排尿记录（饮水/自主排尿/漏尿/间歇导尿） */
@Data
@TableName("urination_record")
public class UrinationRecord {

    @TableId(value = "排尿记录ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    /** 饮水 / 自主排尿 / 漏尿 / 间歇导尿 */
    @TableField("记录类型")
    private String recordType;

    @TableField("发生时间")
    private LocalDateTime occurTime;

    @TableField("饮水量ml")
    private Integer waterMl;

    @TableField("自主排尿量ml")
    private Integer voluntaryMl;

    @TableField("漏尿量ml")
    private Integer leakMl;

    @TableField("导尿量ml")
    private Integer catheterMl;

    @TableField("残余尿量ml")
    private Integer residualMl;

    @TableField("备注")
    private String remark;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    @TableField("最后修改医生ID")
    private Long lastModifyDoctorId;

    @TableField("删除时间")
    private LocalDateTime deleteTime;
}
