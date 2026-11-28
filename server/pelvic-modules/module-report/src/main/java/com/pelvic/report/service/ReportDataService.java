package com.pelvic.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.core.result.PageResult;
import com.pelvic.common.core.util.AesEncryptor;
import com.pelvic.common.security.AuthContext;
import com.pelvic.report.mapper.ReportDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 七张报表（后端实现逻辑.md 第 24 节）：
 * female / male 为 Java 组装（分页患者 + 逐行病史/妊娠/检查聚合）；
 * urination / defecation / assessment / training / summary 为单条聚合 SQL + count。
 */
@Slf4j
@Service
public class ReportDataService {

    public static final String FEMALE = "female";
    public static final String MALE = "male";
    public static final String URINATION = "urination";
    public static final String DEFECATION = "defecation";
    public static final String ASSESSMENT = "assessment";
    public static final String TRAINING = "training";
    public static final String SUMMARY = "summary";

    public static final Set<String> REPORT_TYPES = Set.of(
            FEMALE, MALE, URINATION, DEFECATION, ASSESSMENT, TRAINING, SUMMARY);

    @Autowired
    private ReportDataMapper reportDataMapper;
    @Autowired
    private AesEncryptor aesEncryptor;
    @Autowired
    private ObjectMapper objectMapper;

    public static boolean isSupported(String reportType) {
        return reportType != null && REPORT_TYPES.contains(reportType.trim().toLowerCase(Locale.ROOT));
    }

    public static String normalizeType(String reportType) {
        if (!isSupported(reportType)) {
            throw new BizException("无效的报表类型");
        }
        return reportType.trim().toLowerCase(Locale.ROOT);
    }

    /** 报表分页查询（导出时以 page=1、size=100000 复用全量数据） */
    public PageResult<Map<String, Object>> page(String reportType, Map<String, Object> params) {
        AuthContext.require();
        String type = normalizeType(reportType);
        Map<String, Object> p = params == null ? Collections.emptyMap() : params;

        LocalDate start = ReportSupport.trimToNull(asString(p.get("startDate"))) == null
                ? ReportSupport.defaultStartDate()
                : ReportSupport.parseDate(asString(p.get("startDate")), "开始日期");
        LocalDate end = ReportSupport.trimToNull(asString(p.get("endDate"))) == null
                ? ReportSupport.defaultEndDate()
                : ReportSupport.parseDate(asString(p.get("endDate")), "结束日期");
        ReportSupport.checkRange(start, end);
        String startDate = ReportSupport.format(start);
        String endDate = ReportSupport.format(end);
        String keyword = ReportSupport.trimToNull(asString(p.get("keyword")));

        long page = Math.max(ReportSupport.longValue(p.get("page"), 1), 1);
        long size = ReportSupport.longValue(p.get("size"), 10);
        if (size <= 0) {
            size = 10;
        }
        size = Math.min(size, 100000);
        long offset = (page - 1) * size;

        switch (type) {
            case FEMALE:
                return patientBaseReport("女", keyword, offset, size);
            case MALE:
                return patientBaseReport("男", keyword, offset, size);
            case URINATION: {
                long total = reportDataMapper.countUrinationRows(startDate, endDate, keyword);
                return PageResult.of(total, reportDataMapper.selectUrinationRows(
                        startDate, endDate, keyword, offset, size));
            }
            case DEFECATION: {
                long total = reportDataMapper.countDefecationRows(startDate, endDate, keyword);
                return PageResult.of(total, reportDataMapper.selectDefecationRows(
                        startDate, endDate, keyword, offset, size));
            }
            case ASSESSMENT: {
                long total = reportDataMapper.countAssessmentRows(startDate, endDate, keyword);
                return PageResult.of(total, postProcessAssessment(reportDataMapper.selectAssessmentRows(
                        startDate, endDate, keyword, offset, size)));
            }
            case TRAINING: {
                String trainingType = ReportSupport.trimToNull(asString(p.get("trainingType")));
                if ("全部".equals(trainingType) || "all".equalsIgnoreCase(trainingType)) {
                    trainingType = null;
                }
                long total = reportDataMapper.countTrainingRows(startDate, endDate, keyword, trainingType);
                return PageResult.of(total, postProcessTraining(reportDataMapper.selectTrainingRows(
                        startDate, endDate, keyword, trainingType, offset, size)));
            }
            case SUMMARY: {
                long total = reportDataMapper.countSummaryRows(keyword);
                return PageResult.of(total, postProcessSummary(reportDataMapper.selectSummaryRows(
                        startDate, endDate, keyword, offset, size)));
            }
            default:
                throw new BizException("无效的报表类型");
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    // ==================== female / male：Java 组装 ====================

    private PageResult<Map<String, Object>> patientBaseReport(String gender, String keyword,
                                                              long offset, long size) {
        long total = reportDataMapper.countPatients(gender, keyword);
        List<Map<String, Object>> patients = reportDataMapper.selectPatients(gender, keyword, offset, size);
        List<Map<String, Object>> records = new ArrayList<>(patients.size());
        for (Map<String, Object> patient : patients) {
            records.add("女".equals(gender) ? buildFemaleRow(patient) : buildMaleRow(patient));
        }
        return PageResult.of(total, records);
    }

    private Map<String, Object> buildFemaleRow(Map<String, Object> p) {
        Long patientId = ReportSupport.longVal(p.get("patientId"));
        DetailData d = patientId == null ? new DetailData() : loadDetail(patientId);
        Map<String, Object> h = d.health;
        Map<String, Object> preg = d.pregnancy;
        Map<String, Object> counts = d.pregnancyCounts;
        Map<String, Object> exam = d.exam;

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", ReportSupport.str(p.get("id")));
        row.put("name", ReportSupport.str(p.get("name")));
        row.put("gender", "女");
        row.put("birth", ReportSupport.monthStr(p.get("birth")));
        Integer age = ReportSupport.ageOf(p.get("birth"));
        row.put("age", age == null ? ReportSupport.EMPTY : age);
        row.put("phone", ReportSupport.phone(p.get("phoneCipher"), aesEncryptor, log));
        row.put("height", ReportSupport.withUnit(p.get("height"), " cm"));
        row.put("weight", ReportSupport.withUnit(p.get("weight"), " Kg"));
        row.put("exercise", text(h, "既往运动及生活补充"));
        row.put("marriage", ReportSupport.str(p.get("marriage")));
        row.put("education", ReportSupport.str(p.get("education")));
        row.put("lifestyle", lifestyle(h));
        row.put("job", ReportSupport.str(p.get("job")));
        row.put("mPeriod", unitIfPresent(value(h, "月经持续天数"), " 天"));
        row.put("mCycle", unitIfPresent(value(h, "月经周期天数"), " 天"));
        row.put("dysmenorrhea", vasLabel(value(h, "痛经VAS")));
        row.put("menopause", ReportSupport.yesNo(value(h, "是否绝经")));
        row.put("lastPeriod", ReportSupport.dateStr(value(h, "末次月经日期")));
        row.put("menopauseDate", menopauseDate(h));
        // TODO：DDL 无“女性性功能障碍（ICD-11）”独立字段，v1 返回缺省值。
        row.put("fsd", ReportSupport.EMPTY);
        row.put("stoolInfo", text(h, "大便情况"));
        row.put("urineInfo", text(h, "小便情况"));
        row.put("sexLife", sexLife(h));
        row.put("surgery", ReportSupport.str(d.surgery));
        row.put("pastHistory", summarizeConditions(d.conditions, null));
        row.put("deliveryDate", ReportSupport.dateStr(value(preg, "分娩日期")));
        row.put("pregCount", countLabel(counts, "total"));
        row.put("birthCount", countLabel(counts, "births"));
        row.put("abortionCount", countLabel(counts, "abortions"));
        row.put("deliveryMode", text(preg, "分娩方式"));
        row.put("painless", ReportSupport.yesNo(value(preg, "无痛分娩")));
        row.put("cephalic", ReportSupport.yesNo(value(preg, "头位正常分娩")));
        row.put("assist", text(preg, "助产方式"));
        row.put("fetalWeight", ReportSupport.withUnit(value(preg, "胎儿出生体重kg"), " Kg"));
        row.put("preWeight", ReportSupport.withUnit(value(preg, "孕前体重kg"), " Kg"));
        row.put("anteWeight", ReportSupport.withUnit(value(preg, "产前体重kg"), " Kg"));
        row.put("gainWeight", ReportSupport.withUnit(value(preg, "孕期增重kg"), " Kg"));
        row.put("sleepPos", text(preg, "睡觉体位"));
        row.put("urineIncon", ReportSupport.hasOrNot(value(preg, "尿失禁")));
        row.put("stoolPreg", text(preg, "大便情况"));
        row.put("bp", text(preg, "血压情况"));
        row.put("glucose", text(preg, "血糖情况"));
        row.put("thyroid", text(preg, "甲状腺功能"));
        row.put("backPain", vasLabel(value(preg, "腰背痛VAS")));
        row.put("pubicPain", vasLabel(value(preg, "耻骨疼痛VAS")));
        row.put("legPain", vasLabel(value(preg, "下肢疼痛VAS")));
        row.put("pregnancyReaction", text(preg, "妊娠反应等级"));
        row.put("itch", ReportSupport.hasOrNot(value(preg, "会阴瘙痒")));
        row.put("diet", text(preg, "饮食情况"));
        row.put("sexLifePreg", ReportSupport.hasOrNot(value(preg, "性生活")));
        row.put("chief", firstText(value(exam, "主诉"), value(h, "主诉")));
        row.put("painInfo", summarizePain(d.pain));
        row.put("abdTension", text(exam, "腹部张力"));
        row.put("inspection", d.pelvicInspection != null
                ? summarizePelvic(d.pelvicInspection) : summarizeGeneral(d.generalInspection));
        row.put("specialTest", summarizeSpecial(d.specialExam));
        row.put("nerveTest", summarizeNeurological(d.neurologicalExam));
        row.put("analPalpation", summarizeAnal(d.analPalpation));
        row.put("ultrasound", uploadStatus(d.notes, "盆底超声", "超声"));
        row.put("xray", uploadStatus(d.notes, "X-Ray", "X线", "X光", "DR"));
        row.put("urodynamics", uploadStatus(d.notes, "尿动力学"));
        row.put("otherExam", uploadStatus(d.notes, "其他检查"));
        row.put("diagnosis", text(exam, "诊断"));
        row.put("medication", text(exam, "药物治疗"));
        row.put("supplement", text(exam, "其他补充"));
        return row;
    }

    private Map<String, Object> buildMaleRow(Map<String, Object> p) {
        Long patientId = ReportSupport.longVal(p.get("patientId"));
        DetailData d = patientId == null ? new DetailData() : loadDetail(patientId);
        Map<String, Object> h = d.health;
        Map<String, Object> exam = d.exam;

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", ReportSupport.str(p.get("id")));
        row.put("name", ReportSupport.str(p.get("name")));
        row.put("gender", "男");
        row.put("birth", ReportSupport.monthStr(p.get("birth")));
        Integer age = ReportSupport.ageOf(p.get("birth"));
        row.put("age", age == null ? ReportSupport.EMPTY : age);
        row.put("phone", ReportSupport.phone(p.get("phoneCipher"), aesEncryptor, log));
        row.put("height", ReportSupport.withUnit(p.get("height"), " cm"));
        row.put("weight", ReportSupport.withUnit(p.get("weight"), " Kg"));
        row.put("exercise", text(h, "既往运动及生活补充"));
        row.put("marriage", ReportSupport.str(p.get("marriage")));
        row.put("education", ReportSupport.str(p.get("education")));
        row.put("lifestyle", lifestyle(h));
        row.put("job", ReportSupport.str(p.get("job")));
        row.put("anorectal", summarizeConditions(d.conditions,
                new String[]{"肛", "痔", "直肠", "便秘"}));
        // TODO：DDL 无“男性性功能障碍”独立字段，v1 返回缺省值。
        row.put("msd", ReportSupport.EMPTY);
        row.put("sexLife", sexLife(h));
        row.put("stoolInfo", text(h, "大便情况"));
        row.put("urineInfo", text(h, "小便情况"));
        row.put("surgery", ReportSupport.str(d.surgery));
        row.put("pastHistory", summarizeConditions(d.conditions, null));
        row.put("chief", firstText(value(exam, "主诉"), value(h, "主诉")));
        row.put("drSeparation", summarizeDiastasis(d.diastasis));
        row.put("postpartum", summarizePostpartum(d.postpartum));
        row.put("painInfo", summarizePain(d.pain));
        row.put("inspection", d.generalInspection != null
                ? summarizeGeneral(d.generalInspection) : summarizePelvic(d.pelvicInspection));
        row.put("specialTest", summarizeSpecial(d.specialExam));
        row.put("nerveTest", summarizeNeurological(d.neurologicalExam));
        row.put("palpation", summarizeAnal(d.analPalpation));
        row.put("ultrasound", uploadStatus(d.notes, "盆底超声", "超声"));
        row.put("xray", uploadStatus(d.notes, "X-Ray", "X线", "X光", "DR"));
        row.put("urodynamics", uploadStatus(d.notes, "尿动力学"));
        row.put("otherExam", uploadStatus(d.notes, "其他检查"));
        row.put("diagnosis", text(exam, "诊断"));
        row.put("medication", text(exam, "药物治疗"));
        row.put("supplement", text(exam, "其他补充"));
        return row;
    }

    /** 单行报表所需的病史/妊娠/检查明细（v1 逐行加载，TODO 优化批量查询） */
    private DetailData loadDetail(Long patientId) {
        DetailData d = new DetailData();
        d.health = reportDataMapper.selectHealthProfile(patientId);
        d.pregnancy = reportDataMapper.selectLatestPregnancy(patientId);
        d.pregnancyCounts = reportDataMapper.selectPregnancyCounts(patientId);
        d.surgery = reportDataMapper.selectSurgerySummary(patientId);
        d.conditions = reportDataMapper.selectConditions(patientId);
        d.notes = reportDataMapper.selectClinicalNoteContents(patientId);
        d.exam = reportDataMapper.selectLatestExamination(patientId);
        if (d.exam != null) {
            Long examId = ReportSupport.longVal(d.exam.get("检查ID"));
            if (examId != null) {
                d.diastasis = reportDataMapper.selectDiastasis(examId);
                d.postpartum = reportDataMapper.selectPostpartum(examId);
                d.pain = reportDataMapper.selectPainAssessment(examId);
                d.pelvicInspection = reportDataMapper.selectPelvicInspection(examId);
                d.generalInspection = reportDataMapper.selectGeneralInspection(examId);
                d.specialExam = reportDataMapper.selectSpecialExam(examId);
                d.neurologicalExam = reportDataMapper.selectNeurologicalExam(examId);
                d.analPalpation = reportDataMapper.selectAnalPalpation(examId);
            }
        }
        return d;
    }

    private static final class DetailData {
        private Map<String, Object> health;
        private Map<String, Object> pregnancy;
        private Map<String, Object> pregnancyCounts;
        private String surgery;
        private List<Map<String, Object>> conditions = List.of();
        private List<String> notes = List.of();
        private Map<String, Object> exam;
        private Map<String, Object> diastasis;
        private Map<String, Object> postpartum;
        private Map<String, Object> pain;
        private Map<String, Object> pelvicInspection;
        private Map<String, Object> generalInspection;
        private Map<String, Object> specialExam;
        private Map<String, Object> neurologicalExam;
        private Map<String, Object> analPalpation;
    }

    // ==================== SQL 报表后处理 ====================

    private List<Map<String, Object>> postProcessAssessment(List<Map<String, Object>> rows) {
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> source : rows) {
            Map<String, Object> row = new LinkedHashMap<>(source);
            Object dimJson = row.remove("dimScoresJson");
            row.put("dimScores", dimScores(dimJson));
            row.put("recordId", ReportSupport.str(row.get("recordId")));
            row.put("patientId", ReportSupport.str(row.get("patientId")));
            row.put("name", ReportSupport.str(row.get("name")));
            row.put("scaleName", ReportSupport.str(row.get("scaleName")));
            row.put("fillTime", ReportSupport.str(row.get("fillTime")));
            row.put("totalScore", row.get("totalScore") == null
                    ? ReportSupport.EMPTY : ReportSupport.plain(row.get("totalScore")));
            row.put("result", ReportSupport.str(row.get("result")));
            result.add(row);
        }
        return result;
    }

    private List<Map<String, Object>> postProcessTraining(List<Map<String, Object>> rows) {
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> source : rows) {
            Map<String, Object> row = new LinkedHashMap<>(source);
            Object seconds = row.remove("durationSeconds");
            int sec = ReportSupport.intValue(seconds);
            row.put("duration", sec <= 0 ? ReportSupport.EMPTY
                    : ((sec + 59) / 60) + " 分钟");
            row.put("id", ReportSupport.str(row.get("id")));
            row.put("title", ReportSupport.str(row.get("title")));
            row.put("type", ReportSupport.str(row.get("type")));
            row.put("date", ReportSupport.str(row.get("date")));
            result.add(row);
        }
        return result;
    }

    private List<Map<String, Object>> postProcessSummary(List<Map<String, Object>> rows) {
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> source : rows) {
            Map<String, Object> row = new LinkedHashMap<>(source);
            row.put("id", ReportSupport.str(row.get("id")));
            row.put("name", ReportSupport.str(row.get("name")));
            row.put("statDate", ReportSupport.str(row.get("statDate")));
            row.put("scaleName", ReportSupport.str(row.get("scaleName")));
            row.put("scaleScore", row.get("scaleScore") == null
                    ? ReportSupport.EMPTY : ReportSupport.plain(row.get("scaleScore")));
            row.put("assessDate", ReportSupport.str(row.get("assessDate")));
            row.put("urineCount", ReportSupport.str(row.get("urineCount")));
            row.put("stoolCount", ReportSupport.str(row.get("stoolCount")));
            row.put("abnormalCount", ReportSupport.str(row.get("abnormalCount")));
            row.put("checkinStatus", ReportSupport.str(row.get("checkinStatus")));
            row.put("alertCount", ReportSupport.str(row.get("alertCount")));
            result.add(row);
        }
        return result;
    }

    /** 维度得分快照 JSON → “维度：分；维度：分” */
    private String dimScores(Object json) {
        if (json == null) {
            return ReportSupport.EMPTY;
        }
        String raw = String.valueOf(json).trim();
        if (raw.isEmpty()) {
            return ReportSupport.EMPTY;
        }
        try {
            JsonNode node = objectMapper.readTree(raw);
            List<String> parts = new ArrayList<>();
            collectDimScores(node, parts);
            return parts.isEmpty() ? ReportSupport.EMPTY : String.join("；", parts);
        } catch (Exception e) {
            // 非 JSON 的历史快照：原样返回，避免导出/展示丢数据。
            return raw;
        }
    }

    private void collectDimScores(JsonNode node, List<String> parts) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode value = entry.getValue();
                if (value.isObject()) {
                    String name = nodeText(value.get("维度名称"));
                    if (name == null) {
                        name = nodeText(value.get("维度"));
                    }
                    if (name == null) {
                        name = nodeText(value.get("name"));
                    }
                    JsonNode score = value.get("得分");
                    if (score == null) {
                        score = value.get("score");
                    }
                    if (score == null) {
                        score = value.get("value");
                    }
                    parts.add(name != null && score != null
                            ? name + "：" + score.asText()
                            : entry.getKey() + "：" + value);
                } else {
                    parts.add(entry.getKey() + "：" + value.asText());
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                if (item.isObject()) {
                    String name = nodeText(item.get("维度名称"));
                    if (name == null) {
                        name = nodeText(item.get("维度"));
                    }
                    if (name == null) {
                        name = nodeText(item.get("name"));
                    }
                    JsonNode score = item.get("得分");
                    if (score == null) {
                        score = item.get("score");
                    }
                    if (score == null) {
                        score = item.get("value");
                    }
                    if (name != null && score != null) {
                        parts.add(name + "：" + score.asText());
                    } else {
                        parts.add(item.toString());
                    }
                } else {
                    parts.add(item.asText());
                }
            }
        }
    }

    private String nodeText(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    // ==================== 中文汇总辅助 ====================

    private Object value(Map<String, Object> map, String key) {
        return map == null ? null : map.get(key);
    }

    private String text(Map<String, Object> map, String key) {
        return ReportSupport.text(value(map, key));
    }

    private String firstText(Object first, Object second) {
        String text = ReportSupport.trimToNull(first == null ? null : String.valueOf(first));
        return text != null ? text : ReportSupport.text(second);
    }

    private String unitIfPresent(Object value, String unit) {
        if (value == null || String.valueOf(value).isBlank()) {
            return ReportSupport.EMPTY;
        }
        return ReportSupport.plain(value) + unit;
    }

    private String countLabel(Map<String, Object> counts, String key) {
        if (counts == null) {
            return ReportSupport.EMPTY;
        }
        return ReportSupport.intValue(counts.get(key)) + " 次";
    }

    private String vasLabel(Object value) {
        if (value == null) {
            return ReportSupport.EMPTY;
        }
        int vas = ReportSupport.intValue(value);
        return vas > 0 ? "有（VAS " + vas + "）" : "无";
    }

    private String lifestyle(Map<String, Object> h) {
        if (h == null) {
            return ReportSupport.EMPTY;
        }
        List<String> parts = new ArrayList<>();
        addNumberPart(parts, "抽烟 ", h.get("吸烟支每天"), " 支/天");
        addNumberPart(parts, "喝酒 ", h.get("饮酒ml每天"), " ml/天");
        addNumberPart(parts, "茶/咖啡 ", h.get("茶咖啡ml每天"), " ml/天");
        addNumberPart(parts, "久坐 ", h.get("久坐小时每天"), " h/天");
        addNumberPart(parts, "久站 ", h.get("久站小时每天"), " h/天");
        String other = ReportSupport.trimToNull(h.get("其他习惯") == null ? null : String.valueOf(h.get("其他习惯")));
        if (other != null) {
            parts.add(other);
        }
        return parts.isEmpty() ? ReportSupport.EMPTY : String.join("，", parts);
    }

    private void addNumberPart(List<String> parts, String prefix, Object value, String unit) {
        if (value != null && !String.valueOf(value).isBlank()) {
            parts.add(prefix + ReportSupport.plain(value) + unit);
        }
    }

    private String sexLife(Map<String, Object> h) {
        if (h == null) {
            return ReportSupport.EMPTY;
        }
        List<String> parts = new ArrayList<>();
        Object week = h.get("性生活频率次每周");
        Object month = h.get("性生活频率次每月");
        if (week != null) {
            parts.add("频率 " + ReportSupport.plain(week) + " 次/周");
        } else if (month != null) {
            parts.add("频率 " + ReportSupport.plain(month) + " 次/月");
        }
        Object tourism = h.get("冶游史");
        if (tourism != null) {
            parts.add(ReportSupport.truthy(tourism) ? "有冶游史" : "无冶游史");
        }
        return parts.isEmpty() ? ReportSupport.EMPTY : String.join("，", parts);
    }

    private String menopauseDate(Map<String, Object> h) {
        if (h == null || !ReportSupport.truthy(h.get("是否绝经"))) {
            return ReportSupport.EMPTY;
        }
        Object age = h.get("绝经年龄");
        return age == null ? "已绝经" : "绝经年龄 " + ReportSupport.plain(age) + " 岁";
    }

    private String summarizeConditions(List<Map<String, Object>> conditions, String[] keywords) {
        if (conditions == null || conditions.isEmpty()) {
            return ReportSupport.EMPTY;
        }
        List<String> parts = new ArrayList<>();
        for (Map<String, Object> condition : conditions) {
            String category = ReportSupport.trimToNull(asString(condition.get("项目类别")));
            String name = ReportSupport.trimToNull(asString(condition.get("项目名称")));
            if (name == null) {
                continue;
            }
            if (keywords != null) {
                String haystack = (category == null ? "" : category) + name;
                boolean matched = false;
                for (String keyword : keywords) {
                    if (haystack.contains(keyword)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    continue;
                }
            }
            String detail = ReportSupport.trimToNull(asString(condition.get("详情")));
            parts.add(detail == null ? name : name + "（" + detail + "）");
        }
        return parts.isEmpty() ? ReportSupport.EMPTY : String.join("；", parts);
    }

    /** 体检图片 4 列：v1 按 clinical_note 记录内容是否含关键词且“已上传”判断。 */
    private String uploadStatus(List<String> notes, String... keywords) {
        if (notes == null || notes.isEmpty()) {
            return "未上传";
        }
        for (String note : notes) {
            if (note == null) {
                continue;
            }
            for (String keyword : keywords) {
                if (note.contains(keyword) && note.contains("已上传")) {
                    return "已上传";
                }
            }
        }
        return "未上传";
    }

    private void addPart(List<String> parts, String label, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "-".equals(text) || ReportSupport.EMPTY.equals(text)) {
            return;
        }
        if (value instanceof Number) {
            text = ReportSupport.plain(value);
        }
        parts.add(label + "：" + text);
    }

    private void addBoolPart(List<String> parts, String label, Object value) {
        if (value != null) {
            parts.add(label + "：" + (ReportSupport.truthy(value) ? "是" : "否"));
        }
    }

    private String joinParts(List<String> parts) {
        return parts.isEmpty() ? ReportSupport.EMPTY : String.join("；", parts);
    }

    private String summarizePelvic(Map<String, Object> m) {
        if (m == null) {
            return ReportSupport.EMPTY;
        }
        List<String> parts = new ArrayList<>();
        addPart(parts, "外阴情况", m.get("外阴情况"));
        addPart(parts, "阴道裂口", m.get("阴道裂口"));
        addPart(parts, "尿道口", m.get("尿道口"));
        addPart(parts, "阴蒂", m.get("阴蒂"));
        addBoolPart(parts, "阴唇对称", m.get("阴唇对称"));
        Object secretion = m.get("分泌物情况");
        if (secretion != null) {
            if (ReportSupport.truthy(secretion)) {
                List<String> details = new ArrayList<>();
                addPart(details, "颜色", m.get("分泌物颜色"));
                addPart(details, "气味", m.get("分泌物气味"));
                addPart(details, "质地", m.get("分泌物质地"));
                addPart(details, "量", m.get("分泌物量"));
                parts.add("分泌物：有" + (details.isEmpty() ? "" : "（" + String.join("，", details) + "）"));
            } else {
                parts.add("分泌物：无");
            }
        }
        addPart(parts, "阴毛情况", m.get("阴毛情况"));
        addPart(parts, "痔疮情况", m.get("痔疮情况"));
        addPart(parts, "Valsalva表现", m.get("Valsalva表现"));
        return joinParts(parts);
    }

    private String summarizeGeneral(Map<String, Object> m) {
        if (m == null) {
            return ReportSupport.EMPTY;
        }
        List<String> parts = new ArrayList<>();
        addPart(parts, "精神面貌", m.get("精神面貌"));
        addPart(parts, "步行情况", m.get("步行情况"));
        addPart(parts, "步行异常", m.get("步行异常描述"));
        addPart(parts, "畸形部位", m.get("畸形部位"));
        addPart(parts, "肿胀部位", m.get("肿胀部位"));
        addPart(parts, "疤痕", m.get("疤痕部位及大小"));
        addPart(parts, "瘀斑", m.get("瘀斑部位及大小"));
        addPart(parts, "肛周生殖器肤色", m.get("肛周生殖器肤色"));
        addPart(parts, "肛周皮疹分泌物", m.get("肛周皮疹分泌物"));
        addPart(parts, "毛发分布", m.get("毛发分布"));
        Object prolapse = m.get("直肠脱垂长度cm");
        if (prolapse != null) {
            parts.add("直肠脱垂长度：" + ReportSupport.plain(prolapse) + " cm");
        }
        addPart(parts, "痔疮情况", m.get("痔疮情况"));
        return joinParts(parts);
    }

    private String summarizeSpecial(Map<String, Object> m) {
        if (m == null) {
            return ReportSupport.EMPTY;
        }
        String[][] fields = {
                {"泌尿生殖测试", "泌尿生殖测试"},
                {"疤痕试验", "疤痕试验"},
                {"4字试验左", "4字试验左"},
                {"4字试验右", "4字试验右"},
                {"骨盆分离试验", "骨盆分离试验"},
                {"骨盆挤压试验", "骨盆挤压试验"},
                {"骶骨下压试验", "骶骨下压试验"},
                {"股骨冲击试验", "股骨冲击试验"},
                {"直腿抬高试验", "直腿抬高试验"},
                {"直腿抬高加强试验", "直腿抬高加强试验"},
                {"广泛性触诊试验", "广泛性触诊试验"},
                {"其他", "其他"},
        };
        List<String> parts = new ArrayList<>();
        for (String[] field : fields) {
            addPart(parts, field[0], m.get(field[1]));
        }
        return joinParts(parts);
    }

    private String summarizeNeurological(Map<String, Object> m) {
        if (m == null) {
            return ReportSupport.EMPTY;
        }
        List<String> parts = new ArrayList<>();
        addPart(parts, "盆底张力", m.get("盆底张力"));
        addPart(parts, "左侧感觉", m.get("左侧感觉"));
        addPart(parts, "右侧感觉", m.get("右侧感觉"));
        Object catheter = m.get("导尿管");
        if (catheter != null) {
            parts.add("导尿管：" + (ReportSupport.truthy(catheter) ? "有" : "无"));
        }
        return joinParts(parts);
    }

    private String summarizeAnal(Map<String, Object> m) {
        if (m == null) {
            return ReportSupport.EMPTY;
        }
        List<String> parts = new ArrayList<>();
        addPart(parts, "肛提肌", m.get("肛提肌"));
        addPart(parts, "坐尾肌", m.get("坐尾肌"));
        addPart(parts, "髂尾肌", m.get("髂尾肌"));
        addPart(parts, "闭孔内肌", m.get("闭孔内肌"));
        addPart(parts, "脏器偏移", m.get("脏器偏移"));
        Object oxford = m.get("牛津肌力");
        if (oxford != null) {
            parts.add("牛津肌力：" + ReportSupport.plain(oxford) + " 级");
        }
        Object coccyx = m.get("尾骨嵌顿");
        if (coccyx != null) {
            parts.add("尾骨嵌顿：" + (ReportSupport.truthy(coccyx) ? "有" : "无"));
        }
        addPart(parts, "肛周温度", m.get("肛周温度"));
        addPart(parts, "肛周湿度", m.get("肛周湿度"));
        addPart(parts, "会阴及肛周肌肉张力", m.get("会阴及肛周肌肉张力"));
        return joinParts(parts);
    }

    private String summarizeDiastasis(Map<String, Object> m) {
        if (m == null) {
            return ReportSupport.EMPTY;
        }
        String resting = "静息（脐上 " + plainDash(m.get("静息脐上指宽"))
                + " / 脐 " + plainDash(m.get("静息肚脐指宽"))
                + " / 脐下 " + plainDash(m.get("静息脐下指宽")) + " cm）";
        String contract = "收缩（脐上 " + plainDash(m.get("收缩脐上指宽"))
                + " / 脐 " + plainDash(m.get("收缩肚脐指宽"))
                + " / 脐下 " + plainDash(m.get("收缩脐下指宽")) + " cm）";
        return resting + "；" + contract;
    }

    private String plainDash(Object value) {
        return value == null ? ReportSupport.EMPTY : ReportSupport.plain(value);
    }

    private String summarizePostpartum(Map<String, Object> m) {
        if (m == null) {
            return ReportSupport.EMPTY;
        }
        List<String> parts = new ArrayList<>();
        addPart(parts, "小便情况", m.get("小便情况"));
        addPart(parts, "大便情况", m.get("大便情况"));
        Object pain = m.get("盆腔会阴痛VAS");
        if (pain != null) {
            parts.add("盆腔会阴痛：" + ReportSupport.plain(pain) + " 分");
        }
        addPart(parts, "脏器脱垂", m.get("脏器脱垂"));
        Object scar = m.get("瘢痕情况");
        if (scar != null) {
            if (ReportSupport.truthy(scar)) {
                List<String> details = new ArrayList<>();
                addPart(details, "质地", m.get("瘢痕质地"));
                addPart(details, "颜色", m.get("瘢痕颜色"));
                addPart(details, "时间", m.get("瘢痕时间"));
                Object length = m.get("瘢痕长度cm");
                if (length != null) {
                    details.add("长度 " + ReportSupport.plain(length) + " cm");
                }
                parts.add("瘢痕：有" + (details.isEmpty() ? "" : "（" + String.join("，", details) + "）"));
            } else {
                parts.add("瘢痕：无");
            }
        }
        Object instability = m.get("尿道不稳定性");
        if (instability != null) {
            parts.add("尿道不稳定性：" + (ReportSupport.truthy(instability) ? "有" : "无"));
        }
        addPart(parts, "其他", m.get("其他"));
        return joinParts(parts);
    }

    private String summarizePain(Map<String, Object> m) {
        if (m == null) {
            return ReportSupport.EMPTY;
        }
        String[][] groups = {
                {"腰背痛", "腰背痛左VAS", "腰背痛中VAS", "腰背痛右VAS"},
                {"骶髂关节痛", "骶髂关节痛左VAS", "骶髂关节痛中VAS", "骶髂关节痛右VAS"},
                {"耻骨痛", "耻骨痛VAS"},
                {"尾骨痛", "尾骨痛左VAS", "尾骨痛中VAS", "尾骨痛右VAS"},
                {"坐骨神经痛", "坐骨神经痛左VAS", "坐骨神经痛右VAS"},
                {"腹股沟痛", "腹股沟痛左VAS", "腹股沟痛右VAS"},
                {"颈痛", "颈痛左VAS", "颈痛右VAS"},
                {"上肢痛", "上肢痛左VAS", "上肢痛右VAS"},
                {"下肢痛", "下肢痛左VAS", "下肢痛右VAS"},
        };
        List<String> parts = new ArrayList<>();
        for (String[] group : groups) {
            List<String> values = new ArrayList<>();
            for (int i = 1; i < group.length; i++) {
                int vas = ReportSupport.intValue(m.get(group[i]));
                if (vas > 0) {
                    values.add(sideOf(group[i]) + vas);
                }
            }
            if (!values.isEmpty()) {
                parts.add(group[0] + "（" + String.join("/", values) + "）");
            }
        }
        return joinParts(parts);
    }

    private String sideOf(String column) {
        if (column.contains("左")) {
            return "左";
        }
        if (column.contains("右")) {
            return "右";
        }
        return "中";
    }
}
