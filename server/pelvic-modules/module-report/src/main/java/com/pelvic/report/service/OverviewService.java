package com.pelvic.report.service;

import com.pelvic.common.security.AuthContext;
import com.pelvic.report.dto.OverviewQuery;
import com.pelvic.report.dto.OverviewResponse;
import com.pelvic.report.dto.OverviewRow;
import com.pelvic.report.mapper.AlertMapper;
import com.pelvic.report.mapper.OverviewMapper;
import com.pelvic.report.mapper.TrainingCheckinMapper;
import com.pelvic.report.mapper.UrinationRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据总览（后端实现逻辑.md 第 14、15、17 节）：
 * 患者+日期聚合行分页，补 alert/done 布尔与 KPI（异常预警患者数、今日打卡完成率）。
 */
@Service
public class OverviewService {

    @Autowired
    private OverviewMapper overviewMapper;
    @Autowired
    private UrinationRecordMapper urinationRecordMapper;
    @Autowired
    private AlertMapper alertMapper;
    @Autowired
    private TrainingCheckinMapper trainingCheckinMapper;

    public OverviewResponse list(OverviewQuery query) {
        AuthContext.require();
        OverviewQuery q = query == null ? new OverviewQuery() : query;

        LocalDate start = ReportSupport.trimToNull(q.getStartDate()) == null
                ? ReportSupport.defaultStartDate()
                : ReportSupport.parseDate(q.getStartDate(), "开始日期");
        LocalDate end = ReportSupport.trimToNull(q.getEndDate()) == null
                ? ReportSupport.defaultEndDate()
                : ReportSupport.parseDate(q.getEndDate(), "结束日期");
        ReportSupport.checkRange(start, end);
        String startDate = ReportSupport.format(start);
        String endDate = ReportSupport.format(end);

        String dataStatus = normalizeDataStatus(q.getDataStatus());
        String checkStatus = normalizeCheckStatus(q.getCheckStatus());
        String keyword = ReportSupport.trimToNull(q.getKeyword());

        long page = Math.max(q.getPage(), 1);
        long size = q.getSize() > 0 ? Math.min(q.getSize(), 500) : 30;
        long offset = (page - 1) * size;

        long total = overviewMapper.countRows(startDate, endDate, dataStatus, checkStatus, keyword);
        List<Map<String, Object>> rows = overviewMapper.selectRows(startDate, endDate,
                dataStatus, checkStatus, keyword, offset, size);

        List<OverviewRow> records = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Long patientId = ReportSupport.longVal(row.get("patientId"));
            String date = ReportSupport.dateStr(row.get("date"));

            OverviewRow item = new OverviewRow();
            item.setId(ReportSupport.str(row.get("id")));
            item.setName(ReportSupport.str(row.get("name")));
            item.setDate(date);
            item.setUrine(ReportSupport.intValue(row.get("urine")));
            item.setStool(ReportSupport.intValue(row.get("stool")));
            item.setWater(ReportSupport.intValue(row.get("water")));

            // TODO(v1)：逐行补 alert/done 会有 N+1 查询，数据量上来后合并为聚合 SQL。
            boolean alert = false;
            boolean done = false;
            if (patientId != null) {
                alert = alertMapper.countByPatientAndDate(patientId, date) > 0
                        || urinationRecordMapper.countAbnormalByPatientAndDate(patientId, date) > 0;
                done = trainingCheckinMapper.countDoneByPatientAndDate(patientId, date) > 0;
            }
            item.setAlert(alert);
            item.setDone(done);
            records.add(item);
        }

        OverviewResponse response = new OverviewResponse();
        response.setRecords(records);
        response.setTotal(total);
        response.setAlertCount(alertMapper.countPatientsWithAlert(startDate, endDate));

        long todayStarted = trainingCheckinMapper.countTodayStartedPatients();
        long todayDone = trainingCheckinMapper.countTodayDonePatients();
        double rate = todayStarted == 0 ? 0 : Math.round(todayDone * 1000.0 / todayStarted) / 10.0;
        response.setCheckinRate(rate);
        return response;
    }

    private String normalizeDataStatus(String value) {
        return "alert".equals(value) || "normal".equals(value) ? value : null;
    }

    private String normalizeCheckStatus(String value) {
        return "done".equals(value) || "undone".equals(value) ? value : null;
    }
}
