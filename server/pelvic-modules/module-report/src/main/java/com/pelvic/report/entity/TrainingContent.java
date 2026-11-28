package com.pelvic.report.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 训练内容 */
@Data
@TableName("training_content")
public class TrainingContent {

    @TableId(value = "内容ID", type = IdType.AUTO)
    private Long id;

    @TableField("分类ID")
    private Long categoryId;

    @TableField("内容名称")
    private String name;

    /** 视频 / 音频 / 文本 */
    @TableField("内容形式")
    private String contentType;

    @TableField("内容说明")
    private String description;

    @TableField("内容地址")
    private String contentUrl;

    @TableField("时长秒")
    private Integer durationSeconds;

    @TableField("排序号")
    private Integer sortNo;

    @TableField("是否发布")
    private Integer published;

    @TableField("创建医生ID")
    private Long creatorDoctorId;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
