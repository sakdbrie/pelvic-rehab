package com.pelvic.system.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 系统角色 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(value = "角色ID", type = IdType.AUTO)
    private Long id;

    @TableField("角色编码")
    private String code;

    @TableField("角色名称")
    private String name;

    /** SELF / ALL_PATIENTS / DEPARTMENT / ASSIGNED */
    @TableField("数据范围")
    private String dataScope;

    @TableField("角色状态")
    private String status;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
