package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 手术史 */
@Data
@TableName("surgery_history")
public class SurgeryHistory {

    @TableId(value = "手术史ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    @TableField("手术类别")
    private String surgeryCategory;

    @TableField("手术名称")
    private String surgeryName;

    @TableField("手术日期")
    private LocalDate surgeryDate;

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
