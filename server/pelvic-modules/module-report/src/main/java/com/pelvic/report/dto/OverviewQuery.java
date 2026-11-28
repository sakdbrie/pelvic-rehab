package com.pelvic.report.dto;

import lombok.Data;

/** 数据总览查询参数（GET /admin/v1/report/overview） */
@Data
public class OverviewQuery {

    /** 开始日期 yyyy-MM-dd（缺省近 30 天） */
    private String startDate;

    /** 结束日期 yyyy-MM-dd（缺省今天） */
    private String endDate;

    /** 数据状态：all / alert / normal */
    private String dataStatus;

    /** 打卡状态：all / done / undone */
    private String checkStatus;

    /** 姓名或患者编号关键字 */
    private String keyword;

    private long page = 1;

    private long size = 30;
}
