package com.pelvic.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 训练日程（全局共用） */
@Data
@TableName("training_day")
public class TrainingDay {

    @TableId(value = "训练日程ID", type = IdType.AUTO)
    private Long id;

    @TableField("训练日序号")
    private Integer dayNo;

    @TableField("日程名称")
    private String name;

    @TableField("是否启用")
    private Integer enabled;
}
