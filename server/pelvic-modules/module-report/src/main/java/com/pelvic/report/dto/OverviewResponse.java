package com.pelvic.report.dto;

import lombok.Data;

import java.util.List;

/** 数据总览响应：分页记录 + KPI */
@Data
public class OverviewResponse {

    private List<OverviewRow> records;

    private long total;

    /** 时间范围内存在待处理/处理中预警的患者数 */
    private long alertCount;

    /** 今日打卡完成率（%，0~100） */
    private double checkinRate;
}
