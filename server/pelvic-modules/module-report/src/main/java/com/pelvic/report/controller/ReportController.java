package com.pelvic.report.controller;

import com.pelvic.common.core.result.PageResult;
import com.pelvic.common.core.result.Result;
import com.pelvic.report.dto.ExportSubmitRequest;
import com.pelvic.report.dto.ExportTaskStatus;
import com.pelvic.report.dto.OverviewQuery;
import com.pelvic.report.dto.OverviewResponse;
import com.pelvic.report.dto.RecordsResponse;
import com.pelvic.report.service.ExportTaskService;
import com.pelvic.report.service.OverviewService;
import com.pelvic.report.service.PatientRecordsService;
import com.pelvic.report.service.ReportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** 报表接口（后端实现逻辑.md 第 14~25 节；前缀 /api/admin/v1/report/**） */
@RestController
@RequestMapping("/admin/v1/report")
public class ReportController {

    @Autowired
    private OverviewService overviewService;
    @Autowired
    private PatientRecordsService patientRecordsService;
    @Autowired
    private ReportDataService reportDataService;
    @Autowired
    private ExportTaskService exportTaskService;

    /** 数据总览：分页 + 筛选 + KPI */
    @GetMapping("/overview")
    public Result<OverviewResponse> overview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String dataStatus,
            @RequestParam(required = false) String checkStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "30") long size) {
        OverviewQuery query = new OverviewQuery();
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setDataStatus(dataStatus);
        query.setCheckStatus(checkStatus);
        query.setKeyword(keyword);
        query.setPage(page);
        query.setSize(size);
        return Result.ok(overviewService.list(query));
    }

    /** 患者数据详情：逐日记录 + 打卡明细 + 量表趋势 */
    @GetMapping("/patient/{patientId}/records")
    public Result<RecordsResponse> patientRecords(
            @PathVariable String patientId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "all") String dataStatus) {
        return Result.ok(patientRecordsService.get(patientId, startDate, endDate, dataStatus));
    }

    /** 七张报表通用分页查询（reportType：female/male/urination/defecation/assessment/training/summary） */
    @GetMapping("/{reportType}")
    public Result<PageResult<Map<String, Object>>> report(
            @PathVariable String reportType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trainingType,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("keyword", keyword);
        params.put("trainingType", trainingType);
        params.put("page", page);
        params.put("size", size);
        return Result.ok(reportDataService.page(reportType, params));
    }

    /** 提交异步导出任务，返回 { taskId } */
    @PostMapping("/export-task")
    public Result<Map<String, Object>> submitExportTask(@RequestBody ExportSubmitRequest request) {
        String taskId = exportTaskService.submit(request.getReportType(), request.getParams());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", taskId);
        return Result.ok(data);
    }

    /** 轮询导出任务状态 */
    @GetMapping("/export-task/{taskId}")
    public Result<ExportTaskStatus> exportTaskStatus(@PathVariable String taskId) {
        return Result.ok(exportTaskService.status(taskId));
    }

    /** 下载导出文件 */
    @GetMapping("/export-task/{taskId}/download")
    public ResponseEntity<Resource> downloadExportTask(@PathVariable String taskId) {
        return exportTaskService.download(taskId);
    }
}
