package com.pelvic.report.dto;

import lombok.Data;

/** 数据总览行（患者 + 日期维度） */
@Data
public class OverviewRow {

    /** 患者编号，如 P001 */
    private String id;

    /** 日期 yyyy-MM-dd */
    private String date;

    private String name;

    private int urine;

    private int stool;

    private int water;

    /** 当日存在未处理预警或漏尿/导尿记录 */
    private boolean alert;

    /** 当日有已完成打卡 */
    private boolean done;
}
