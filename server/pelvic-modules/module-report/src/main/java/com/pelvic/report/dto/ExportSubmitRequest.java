package com.pelvic.report.dto;

import lombok.Data;

import java.util.Map;

/** 提交异步导出任务请求体 */
@Data
public class ExportSubmitRequest {

    /** female / male / urination / defecation / assessment / training / summary（Service 层校验必填） */
    private String reportType;

    /** 当前筛选条件（时间范围、关键字、训练类型等） */
    private Map<String, Object> params;
}
