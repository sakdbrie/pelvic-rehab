package com.pelvic.report.service;

import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.security.AuthContext;
import com.pelvic.report.dto.CheckinItem;
import com.pelvic.report.dto.RecordRow;
import com.pelvic.report.dto.RecordsResponse;
import com.pelvic.report.dto.ScaleInfo;
import com.pelvic.report.dto.ScalePoint;
import com.pelvic.report.entity.QuestionnaireSubmission;
import com.pelvic.report.entity.TrainingCheckin;
import com.pelvic.report.entity.TrainingContent;
import com.pelvic.report.mapper.AlertMapper;
import com.pelvic.report.mapper.DefecationRecordMapper;
import com.pelvic.report.mapper.PatientRefMapper;
import com.pelvic.report.mapper.QuestionnaireMapper;
import com.pelvic.report.mapper.QuestionnaireSubmissionMapper;
import com.pelvic.report.mapper.TrainingCheckinMapper;
import com.pelvic.report.mapper.TrainingContentMapper;
import com.pelvic.report.mapper.UrinationRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 患者数据详情（后端实现逻辑.md 第 18、19、20 节）：
 * 逐日聚合排尿/排便/饮水/异常/打卡，附打卡明细、9 个量表元信息与各量表最近 5 次提交趋势。
 */
@Service
public class PatientRecordsService {

    @Autowired
    private PatientRefMapper patientRefMapper;
    @Autowired
    private UrinationRecordMapper urinationRecordMapper;
    @Autowired
    private DefecationRecordMapper defecationRecordMapper;
    @Autowired
    private AlertMapper alertMapper;
    @Autowired
    private TrainingCheckinMapper trainingCheckinMapper;
    @Autowired
    private TrainingContentMapper trainingContentMapper;
    @Autowired
    private QuestionnaireSubmissionMapper questionnaireSubmissionMapper;
    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    public RecordsResponse get(String patientNo, String startDateValue, String endDateValue, String dataStatusValue) {
        AuthContext.require();
        Long patientId = patientRefMapper.selectIdByNo(patientNo);
        if (patientId == null) {
            throw new BizException("患者不存在");
        }

        LocalDate start = ReportSupport.trimToNull(startDateValue) == null
                ? ReportSupport.defaultStartDate()
                : ReportSupport.parseDate(startDateValue, "开始日期");
        LocalDate end = ReportSupport.trimToNull(endDateValue) == null
                ? ReportSupport.defaultEndDate()
                : ReportSupport.parseDate(endDateValue, "结束日期");
        ReportSupport.checkRange(start, end);
        String dataStatus = "alert".equals(dataStatusValue) || "normal".equals(dataStatusValue)
                ? dataStatusValue : "all";

        List<RecordRow> records = new ArrayList<>();
        Map<String, List<CheckinItem>> checkins = new LinkedHashMap<>();

        // TODO(v1)：逐日逐指标查询，患者/区间较大时为 N+1；后续可改为一次 GROUP BY 聚合。
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            String date = ReportSupport.format(day);

            int urine = urinationRecordMapper.countVoidByPatientAndDate(patientId, date);
            int stool = defecationRecordMapper.countByPatientAndDate(patientId, date);
            int water = urinationRecordMapper.sumWaterByPatientAndDate(patientId, date);
            boolean alert = alertMapper.countByPatientAndDate(patientId, date) > 0
                    || urinationRecordMapper.countAbnormalByPatientAndDate(patientId, date) > 0;
            boolean done = trainingCheckinMapper.countDoneByPatientAndDate(patientId, date) > 0;
            String scaleName = latestScaleName(patientId, date);

            if (!filteredOut(dataStatus, alert)) {
                RecordRow row = new RecordRow();
                row.setDate(date);
                row.setUrine(urine);
                row.setStool(stool);
                row.setWater(water);
                row.setIsAlert(alert);
                row.setIsDone(done);
                row.setScaleName(scaleName);
                records.add(row);
            }

            List<TrainingCheckin> dayCheckins = trainingCheckinMapper.selectByPatientAndDate(patientId, date);
            if (!dayCheckins.isEmpty()) {
                List<CheckinItem> items = new ArrayList<>(dayCheckins.size());
                for (TrainingCheckin checkin : dayCheckins) {
                    TrainingContent content = checkin.getContentId() == null
                            ? null : trainingContentMapper.selectById(checkin.getContentId());
                    CheckinItem item = new CheckinItem();
                    item.setTime(ReportSupport.timeStr(checkin.getStartPlayTime()));
                    item.setTitle(content != null ? content.getName() : "训练内容");
                    item.setSub(subtitle(content));
                    item.setDone(true);
                    items.add(item);
                }
                checkins.put(date, items);
            }
        }

        List<ScaleInfo> scales = scales();
        Map<String, List<ScalePoint>> scaleHistory = new LinkedHashMap<>();
        for (ScaleInfo scale : scales) {
            scaleHistory.put(scale.getKey(), history(patientId, scale));
        }

        RecordsResponse response = new RecordsResponse();
        response.setPatientName(patientRefMapper.selectNameByNo(patientNo));
        response.setRecords(records);
        response.setCheckins(checkins);
        response.setScales(scales);
        response.setScaleHistory(scaleHistory);
        return response;
    }

    private boolean filteredOut(String dataStatus, boolean alert) {
        if ("alert".equals(dataStatus)) {
            return !alert;
        }
        if ("normal".equals(dataStatus)) {
            return alert;
        }
        return false;
    }

    private String latestScaleName(Long patientId, String date) {
        QuestionnaireSubmission latest = questionnaireSubmissionMapper
                .selectLatestByPatientAndDate(patientId, date);
        if (latest == null || latest.getQuestionnaireId() == null) {
            return null;
        }
        return questionnaireMapper.selectNameById(latest.getQuestionnaireId());
    }

    private String subtitle(TrainingContent content) {
        if (content == null) {
            return "训练 · —";
        }
        String form = switch (content.getContentType() == null ? "" : content.getContentType()) {
            case "视频" -> "视频";
            case "音频" -> "音频";
            case "文本" -> "图文";
            default -> "训练";
        };
        Integer seconds = content.getDurationSeconds();
        if (seconds == null) {
            return form + " · —";
        }
        int minutes = seconds <= 0 ? 0 : (seconds + 59) / 60;
        return form + " · " + minutes + "分钟";
    }

    private List<ScalePoint> history(Long patientId, ScaleInfo scale) {
        List<ScalePoint> points = new ArrayList<>();
        Map<String, Object> ref = questionnaireMapper.selectRefByName(scale.getName());
        if (ref == null) {
            // v1 量表尚未 seed 时优雅降级：返回空列表，前端不绘制该量表曲线。
            return points;
        }
        Long questionnaireId = ReportSupport.longVal(ref.get("id"));
        if (questionnaireId == null) {
            return points;
        }
        List<QuestionnaireSubmission> submissions =
                questionnaireSubmissionMapper.selectHistory(patientId, questionnaireId);
        // 查询按提交时间倒序（最近 5 次），前端折线图需要按时间正序。
        for (int i = submissions.size() - 1; i >= 0; i--) {
            QuestionnaireSubmission submission = submissions.get(i);
            BigDecimal total = submission.getTotalScore();
            double percent = total == null ? 0
                    : (double) Math.round(total.doubleValue() * 100.0 / scale.getMax());

            ScalePoint point = new ScalePoint();
            point.setDate(ReportSupport.dateStr(submission.getSubmitTime()));
            point.setScore(percent);
            point.setName(scale.getName());
            point.setMax(scale.getMax());
            points.add(point);
        }
        return points;
    }

    /** 9 个固定量表（与前端 PatientDataDetailView.vue 的 SCALES 常量一致） */
    private List<ScaleInfo> scales() {
        List<ScaleInfo> list = new ArrayList<>(9);
        list.add(new ScaleInfo("pfdi20", "PFDI-20", 300));
        list.add(new ScaleInfo("pisq12", "PISQ-12", 48));
        list.add(new ScaleInfo("iiq7", "IIQ-7", 21));
        list.add(new ScaleInfo("pfmuscle", "盆底肌力", 100));
        list.add(new ScaleInfo("fsfi", "FSFI", 36));
        list.add(new ScaleInfo("pacsym", "PAC-SYM", 60));
        list.add(new ScaleInfo("iciq", "ICI-Q-SF", 21));
        list.add(new ScaleInfo("oabq", "OAB-q", 100));
        list.add(new ScaleInfo("pfiq7", "PFIQ-7", 300));
        return list;
    }
}
