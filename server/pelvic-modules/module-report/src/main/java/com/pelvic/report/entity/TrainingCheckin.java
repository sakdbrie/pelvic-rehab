package com.pelvic.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 训练打卡（无创建/更新时间列） */
@Data
@TableName("training_checkin")
public class TrainingCheckin {

    @TableId(value = "打卡ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    @TableField("内容ID")
    private Long contentId;

    @TableField("训练日程ID")
    private Long trainingDayId;

    @TableField("开始播放时间")
    private LocalDateTime startPlayTime;

    @TableField("完成时间")
    private LocalDateTime completeTime;

    @TableField("播放进度百分比")
    private BigDecimal progressPercent;

    /** 进行中 / 已完成 */
    @TableField("打卡状态")
    private String checkinStatus;
}
