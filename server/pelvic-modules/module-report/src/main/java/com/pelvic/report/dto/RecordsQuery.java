package com.pelvic.report.dto;

import lombok.Data;

/** 患者数据详情查询参数（GET /admin/v1/report/patient/{patientId}/records） */
@Data
public class RecordsQuery {

    /** 开始日期 yyyy-MM-dd */
    private String startDate;

    /** 结束日期 yyyy-MM-dd */
    private String endDate;

    /** 数据状态：all / alert / normal */
    private String dataStatus;
}
