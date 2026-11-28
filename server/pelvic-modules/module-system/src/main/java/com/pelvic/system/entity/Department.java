package com.pelvic.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 科室 */
@Data
@TableName("department")
public class Department {

    @TableId(value = "科室ID", type = IdType.AUTO)
    private Long id;

    @TableField("科室名称")
    private String name;

    @TableField("科室说明")
    private String description;
}
