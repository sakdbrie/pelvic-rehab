package com.pelvic.report.dto;

import lombok.Data;

/** 打卡日历明细项 */
@Data
public class CheckinItem {

    /** 开始播放时间 HH:mm */
    private String time;

    /** 训练内容名称 */
    private String title;

    /** 形式 + 时长，如 “视频 · 3分钟” */
    private String sub;

    private boolean done;
}
