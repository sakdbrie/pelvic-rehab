package com.pelvic.report.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 自评量表定义 */
@Data
@TableName("questionnaire")
public class Questionnaire {

    @TableId(value = "量表ID", type = IdType.AUTO)
    private Long id;

    @TableField("量表编码")
    private String code;

    @TableField("量表名称")
    private String name;

    @TableField("量表说明")
    private String description;

    @TableField("计分说明")
    private String scoringDescription;

    /** 草稿 / 已发布 / 停用 */
    @TableField("状态")
    private String status;

    @TableField("发布时间")
    private LocalDateTime publishTime;

    @TableField("创建人账号ID")
    private Long creatorAccountId;

    @TableField("逻辑删除")
    private Integer deleted;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
