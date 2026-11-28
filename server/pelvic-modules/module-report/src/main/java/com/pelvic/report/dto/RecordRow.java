package com.pelvic.report.dto;

import lombok.Data;

/** 患者数据详情：每日记录行 */
@Data
public class RecordRow {

    /** 日期 yyyy-MM-dd */
    private String date;

    private int urine;

    private int stool;

    private int water;

    /** 当日异常（未处理预警或漏尿/导尿） */
    private Boolean isAlert;

    /** 当日有已完成打卡 */
    private Boolean isDone;

    /** 当日最近提交的量表名称，无则 null */
    private String scaleName;
}
