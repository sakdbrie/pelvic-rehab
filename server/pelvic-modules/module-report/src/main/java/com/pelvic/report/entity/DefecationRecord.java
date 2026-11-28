package com.pelvic.report.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 排便记录 */
@Data
@TableName("defecation_record")
public class DefecationRecord {

    @TableId(value = "排便记录ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    @TableField("发生时间")
    private LocalDateTime occurTime;

    /** 成型 / 稀便 / 水样便 */
    @TableField("大便性状")
    private String stoolType;

    /** 慢 / 正常 / 快 */
    @TableField("排便速度")
    private String speed;

    /** 少 / 正常 / 多 */
    @TableField("排便量")
    private String volume;

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
