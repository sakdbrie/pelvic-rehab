package com.pelvic.report.dto;

import lombok.Data;

/** 导出任务状态（内存任务表） */
@Data
public class ExportTaskStatus {

    public static final String PROCESSING = "PROCESSING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";

    /** 异步线程写、请求线程读，字段用 volatile 保证可见性 */
    private volatile String taskId;

    private volatile String reportType;

    /** PROCESSING / SUCCESS / FAILED */
    private volatile String status;

    private volatile String fileName;

    /** 完成后可下载地址 */
    private volatile String downloadUrl;

    private volatile String errorMessage;
}
