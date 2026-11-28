package com.pelvic.report.service;

import com.alibaba.excel.EasyExcel;
import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.security.AuthContext;
import com.pelvic.report.dto.ExportTaskStatus;
import com.pelvic.system.entity.SysOperateLog;
import com.pelvic.system.mapper.SysOperateLogMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步导出（后端实现逻辑.md 第 25 节）：
 * 内存任务表 + 单线程执行器；EasyExcel 动态表头写本地文件；完成后提供下载接口。
 * 说明：v1 以本地磁盘替代对象存储，任务表不持久化（重启后丢失），后续可替换为 DB + OSS。
 */
@Slf4j
@Service
public class ExportTaskService {

    private static final String MODULE = "module-report";
    private static final String RESOURCE_TYPE = "report";
    private static final String SUBMIT_URI = "/api/admin/v1/report/export-task";
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ==================== 导出列定义（key 与报表行字段严格一致） ====================

    private static final String[] FEMALE_KEYS = {
            "id", "name", "gender", "birth", "age", "phone", "height", "weight", "exercise",
            "marriage", "education", "lifestyle", "job", "mPeriod", "mCycle", "dysmenorrhea",
            "menopause", "lastPeriod", "menopauseDate", "fsd", "stoolInfo", "urineInfo", "sexLife",
            "surgery", "pastHistory", "deliveryDate", "pregCount", "birthCount", "abortionCount",
            "deliveryMode", "painless", "cephalic", "assist", "fetalWeight", "preWeight", "anteWeight",
            "gainWeight", "sleepPos", "urineIncon", "stoolPreg", "bp", "glucose", "thyroid",
            "backPain", "pubicPain", "legPain", "pregnancyReaction", "itch", "diet", "sexLifePreg",
            "chief", "painInfo", "abdTension", "inspection", "specialTest", "nerveTest",
            "analPalpation", "ultrasound", "xray", "urodynamics", "otherExam", "diagnosis",
            "medication", "supplement"
    };

    private static final String[] FEMALE_HEADERS = {
            "患者编号", "姓名", "性别", "出生年月", "年龄", "联系电话", "身高(cm)", "体重(kg)",
            "既往运动情况", "婚姻状况", "教育程度", "生活习惯", "从事职业", "月经持续时间",
            "月经周期", "痛经", "已绝经", "末次月经", "绝经时间", "女性性功能障碍（ICD-11分类）",
            "大便情况", "小便情况", "性生活", "手术史", "既往史", "分娩日期", "怀孕次数",
            "分娩次数", "流产次数", "分娩方式", "无痛分娩", "头位分娩", "助产方式", "胎儿出生体重",
            "孕前体重", "产前体重", "孕期增重", "睡觉体位", "尿失禁", "大便", "血压", "血糖",
            "甲状腺功能", "腰背痛", "耻骨疼痛", "下肢疼痛", "妊娠反应", "会阴瘙痒", "饮食情况",
            "性生活", "主诉", "疼痛情况", "腹部张力", "视诊", "特殊检查", "神经损伤查体",
            "肛门触诊", "盆底超声（可上传照片）", "X-Ray检查（可上传照片）",
            "尿动力学检查（可上传照片）", "其他检查（可上传照片）", "诊断", "药物治疗",
            "其它补充（可上传照片）"
    };

    private static final String[] MALE_KEYS = {
            "id", "name", "gender", "birth", "age", "phone", "height", "weight", "exercise",
            "marriage", "education", "lifestyle", "job", "anorectal", "msd", "sexLife",
            "stoolInfo", "urineInfo", "surgery", "pastHistory", "chief", "drSeparation",
            "postpartum", "painInfo", "inspection", "specialTest", "nerveTest", "palpation",
            "ultrasound", "xray", "urodynamics", "otherExam", "diagnosis", "medication", "supplement"
    };

    private static final String[] MALE_HEADERS = {
            "患者编号", "姓名", "性别", "出生年月", "年龄", "联系电话", "身高(cm)", "体重(kg)",
            "既往运动情况", "婚姻状况", "教育程度", "生活习惯", "从事职业", "肛肠史",
            "男性性功能障碍", "性生活", "大便情况", "小便情况", "手术史", "既往史", "主诉",
            "腹直肌分离", "产后情况", "疼痛情况", "盆底视诊", "特殊检查", "神经损伤查体", "触诊",
            "盆底超声（可上传照片）", "X-Ray检查（可上传照片）", "尿动力学检查（可上传照片）",
            "其他检查（可上传照片）", "诊断", "药物治疗", "其它补充（可上传照片）"
    };

    private static final String[] URINATION_KEYS = {
            "id", "name", "date", "type", "water", "voidVolume", "leakVolume",
            "catheterVolume", "other", "recordTime", "totalCount"
    };
    private static final String[] URINATION_HEADERS = {
            "患者编号", "患者姓名", "记录日期", "记录类型", "饮水量(ml)", "排尿量(ml)",
            "漏尿量(ml)", "导尿量（ml）", "其他", "录入时间", "当日排尿总次数"
    };

    private static final String[] DEFECATION_KEYS = {
            "id", "name", "date", "stoolType", "speed", "volume", "other", "recordTime"
    };
    private static final String[] DEFECATION_HEADERS = {
            "患者编号", "患者姓名", "记录日期", "大便类型", "排便速度", "排便量", "其他", "录入时间"
    };

    private static final String[] ASSESSMENT_KEYS = {
            "recordId", "patientId", "name", "scaleName", "fillTime",
            "totalScore", "dimScores", "result"
    };
    private static final String[] ASSESSMENT_HEADERS = {
            "记录唯一ID", "患者唯一ID", "患者姓名", "量表名称", "填写时间",
            "总得分", "各维度得分", "评估结果"
    };

    private static final String[] TRAINING_KEYS = {
            "id", "title", "type", "date", "duration"
    };
    private static final String[] TRAINING_HEADERS = {
            "患者编号", "训练项目名称", "训练类型", "打卡日期", "训练时长(分钟)"
    };

    private static final String[] SUMMARY_KEYS = {
            "id", "name", "statDate", "scaleName", "scaleScore", "assessDate",
            "urineCount", "stoolCount", "abnormalCount", "checkinStatus", "alertCount"
    };
    private static final String[] SUMMARY_HEADERS = {
            "患者编号", "患者姓名", "统计开始日期", "自评表名称", "自评表得分", "评估日期",
            "排尿日记（每日记录次数）", "排便日记", "异常（漏尿、导尿）排尿次数",
            "训练打卡完成情况", "异常预警次数"
    };

    @Autowired
    private ReportDataService reportDataService;
    @Autowired
    private SysOperateLogMapper sysOperateLogMapper;

    @Value("${pelvic.report.export-dir:./export}")
    private String exportDir;

    /** 内存任务表：taskId → 状态（TODO：后续持久化到 DB） */
    private final Map<String, ExportTaskStatus> tasks = new ConcurrentHashMap<>();

    private ExecutorService executor;

    @PostConstruct
    public void init() {
        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "report-export");
            thread.setDaemon(true);
            return thread;
        });
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /** 提交导出任务，返回 taskId；审计日志同步写入 sys_operate_log */
    public String submit(String reportType, Map<String, Object> params) {
        AuthContext context = AuthContext.require();
        if (reportType == null || reportType.isBlank()) {
            throw new BizException("报表类型不能为空");
        }
        String type = ReportDataService.normalizeType(reportType);

        String taskId = UUID.randomUUID().toString();
        ExportTaskStatus status = new ExportTaskStatus();
        status.setTaskId(taskId);
        status.setReportType(type);
        status.setStatus(ExportTaskStatus.PROCESSING);
        tasks.put(taskId, status);

        writeOperateLog(context.getAccountId(), type, params);

        Map<String, Object> query = params == null
                ? new LinkedHashMap<>() : new LinkedHashMap<>(params);
        executor.submit(() -> runExport(context, taskId, type, query));
        return taskId;
    }

    public ExportTaskStatus status(String taskId) {
        AuthContext.require();
        ExportTaskStatus status = tasks.get(taskId);
        if (status == null) {
            throw new BizException("导出任务不存在");
        }
        return status;
    }

    /** 下载已完成任务的 Excel 文件 */
    public ResponseEntity<Resource> download(String taskId) {
        AuthContext.require();
        ExportTaskStatus status = status(taskId);
        if (!ExportTaskStatus.SUCCESS.equals(status.getStatus())) {
            throw new BizException(ExportTaskStatus.FAILED.equals(status.getStatus())
                    ? "导出任务失败：" + status.getErrorMessage() : "导出任务尚未完成");
        }
        if (status.getFileName() == null) {
            throw new BizException("导出文件不存在");
        }
        File file = new File(exportDir, status.getFileName());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        String encoded = URLEncoder.encode(status.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
    }

    /** 异步执行：全量查询 → EasyExcel 写文件 → 更新任务状态 */
    private void runExport(AuthContext context, String taskId, String type, Map<String, Object> params) {
        ExportTaskStatus status = tasks.get(taskId);
        AuthContext.set(context);
        try {
            Map<String, Object> query = new LinkedHashMap<>(params);
            query.put("page", 1);
            query.put("size", 100000);
            List<Map<String, Object>> rows = reportDataService.page(type, query).getRecords();

            Columns columns = columns(type);
            List<List<String>> head = new ArrayList<>(columns.headers.length);
            for (String header : columns.headers) {
                head.add(List.of(header));
            }
            List<List<Object>> data = new ArrayList<>(rows.size());
            for (Map<String, Object> row : rows) {
                List<Object> line = new ArrayList<>(columns.keys.length);
                for (String key : columns.keys) {
                    line.add(row.get(key));
                }
                data.add(line);
            }

            File directory = new File(exportDir);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("无法创建导出目录：" + directory.getAbsolutePath());
            }
            String fileName = type + "_" + LocalDateTime.now().format(FILE_TIME)
                    + "_" + taskId.substring(0, 8) + ".xlsx";
            File file = new File(directory, fileName);
            EasyExcel.write(file).head(head).sheet("数据").doWrite(data);

            status.setFileName(fileName);
            status.setDownloadUrl("/api/admin/v1/report/export-task/" + taskId + "/download");
            status.setStatus(ExportTaskStatus.SUCCESS);
            log.info("报表导出完成 taskId={} type={} rows={}", taskId, type, rows.size());
        } catch (Exception e) {
            log.error("报表导出失败 taskId={} type={}", taskId, type, e);
            status.setStatus(ExportTaskStatus.FAILED);
            String message = e.getMessage();
            status.setErrorMessage(message == null ? "导出失败" : message);
        } finally {
            AuthContext.clear();
        }
    }

    private Columns columns(String type) {
        return switch (type) {
            case ReportDataService.FEMALE -> new Columns(FEMALE_KEYS, FEMALE_HEADERS);
            case ReportDataService.MALE -> new Columns(MALE_KEYS, MALE_HEADERS);
            case ReportDataService.URINATION -> new Columns(URINATION_KEYS, URINATION_HEADERS);
            case ReportDataService.DEFECATION -> new Columns(DEFECATION_KEYS, DEFECATION_HEADERS);
            case ReportDataService.ASSESSMENT -> new Columns(ASSESSMENT_KEYS, ASSESSMENT_HEADERS);
            case ReportDataService.TRAINING -> new Columns(TRAINING_KEYS, TRAINING_HEADERS);
            case ReportDataService.SUMMARY -> new Columns(SUMMARY_KEYS, SUMMARY_HEADERS);
            default -> throw new BizException("无效的报表类型");
        };
    }

    private void writeOperateLog(Long accountId, String reportType, Map<String, Object> params) {
        SysOperateLog operateLog = new SysOperateLog();
        operateLog.setAccountId(accountId);
        operateLog.setModule(MODULE);
        operateLog.setResourceType(RESOURCE_TYPE);
        operateLog.setResourceId(reportType);
        operateLog.setAction("EXPORT");
        operateLog.setResult("SUCCESS");
        operateLog.setMethod("POST");
        operateLog.setUri(SUBMIT_URI);
        operateLog.setParams(params == null ? null : params.toString());
        sysOperateLogMapper.insert(operateLog);
    }

    private static final class Columns {
        private final String[] keys;
        private final String[] headers;

        private Columns(String[] keys, String[] headers) {
            this.keys = keys;
            this.headers = headers;
        }
    }
}
