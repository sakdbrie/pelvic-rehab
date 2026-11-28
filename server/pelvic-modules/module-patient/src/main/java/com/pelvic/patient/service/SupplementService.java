package com.pelvic.patient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.security.AuthContext;
import com.pelvic.patient.dto.SupplementRequest;
import com.pelvic.patient.entity.AnalPalpation;
import com.pelvic.patient.entity.ClinicalNote;
import com.pelvic.patient.entity.Diastasis;
import com.pelvic.patient.entity.Examination;
import com.pelvic.patient.entity.NeurologicalExam;
import com.pelvic.patient.entity.PainAssessment;
import com.pelvic.patient.entity.Patient;
import com.pelvic.patient.entity.PelvicInspection;
import com.pelvic.patient.entity.Postpartum;
import com.pelvic.patient.entity.SpecialExam;
import com.pelvic.patient.mapper.AnalPalpationMapper;
import com.pelvic.patient.mapper.ClinicalNoteMapper;
import com.pelvic.patient.mapper.DiastasisMapper;
import com.pelvic.patient.mapper.ExaminationMapper;
import com.pelvic.patient.mapper.NeurologicalExamMapper;
import com.pelvic.patient.mapper.PainAssessmentMapper;
import com.pelvic.patient.mapper.PatientMapper;
import com.pelvic.patient.mapper.PelvicInspectionMapper;
import com.pelvic.patient.mapper.PostpartumMapper;
import com.pelvic.patient.mapper.SpecialExamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 医生“补充”临床检查（后端实现逻辑.md 第 10.5 节）：
 * 新建 examination 主记录，并按表单内容落各检查子表；所有解析失败均按列默认值处理，不抛异常。
 */
@Service
public class SupplementService {

    private static final Set<String> URINE_VALUES = Set.of("无", "压力性", "急迫性", "充溢性", "混合性");
    private static final Set<String> STOOL_VALUES = Set.of("正常", "失禁", "便秘");
    private static final Set<String> PELVIC_TONE_VALUES = Set.of("正常", "增高", "降低");
    private static final Set<String> SENSATION_VALUES = Set.of("正常", "减弱", "过敏");

    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private ExaminationMapper examinationMapper;
    @Autowired
    private DiastasisMapper diastasisMapper;
    @Autowired
    private PostpartumMapper postpartumMapper;
    @Autowired
    private PainAssessmentMapper painAssessmentMapper;
    @Autowired
    private PelvicInspectionMapper pelvicInspectionMapper;
    @Autowired
    private SpecialExamMapper specialExamMapper;
    @Autowired
    private NeurologicalExamMapper neurologicalExamMapper;
    @Autowired
    private AnalPalpationMapper analPalpationMapper;
    @Autowired
    private ClinicalNoteMapper clinicalNoteMapper;

    /**
     * 保存补充检查。
     *
     * @param patientId 患者编号（如 P20260001），不是数据库主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(String patientId, SupplementRequest req) {
        // TODO 数据范围校验：医生且对该患者有权限（授权模式后续实现）
        Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getPatientNo, patientId)
                .eq(Patient::getDeleted, 0)
                .last("LIMIT 1"));
        if (patient == null) {
            throw new BizException("患者不存在");
        }
        Long doctorId = patientMapper.selectDoctorId(AuthContext.require().getAccountId());
        if (doctorId == null) {
            throw new BizException("当前账号无医生档案，无法补充检查");
        }

        // 1. 检查主记录
        Examination examination = new Examination();
        examination.setPatientId(patient.getId());
        examination.setDoctorId(doctorId);
        examination.setLastModifyDoctorId(doctorId);
        examination.setExamTime(LocalDateTime.now());
        examination.setChief(blankToNull(req.getChief()));
        examination.setDiagnosis(blankToNull(req.getDiagnosis()));
        examination.setMedication(buildMedication(req));
        examination.setOtherSupplement(blankToNull(req.getSupplement()));
        // 表单无腹部张力字段
        examination.setAbdominalTension(null);
        examinationMapper.insert(examination);
        Long examinationId = examination.getId();

        // 2. 腹直肌分离：仅“有”时创建
        if ("有".equals(trim(req.getDrSeparation()))) {
            Diastasis diastasis = new Diastasis();
            diastasis.setExaminationId(examinationId);
            diastasis.setRestingUmbilicus(decimalOrZero(req.getDrValues(), "静息·肚脐"));
            diastasis.setRestingAbove(decimalOrZero(req.getDrValues(), "静息·脐上"));
            diastasis.setRestingBelow(decimalOrZero(req.getDrValues(), "静息·脐下"));
            diastasis.setContractionUmbilicus(decimalOrZero(req.getDrValues(), "收缩·肚脐"));
            diastasis.setContractionAbove(decimalOrZero(req.getDrValues(), "收缩·脐上"));
            diastasis.setContractionBelow(decimalOrZero(req.getDrValues(), "收缩·脐下"));
            diastasisMapper.insert(diastasis);
        }

        // 3. 产后情况（始终创建）
        savePostpartum(examinationId, req.getPostpartum());

        // 4. 疼痛评估（始终创建，20 个 VAS）
        savePainAssessment(examinationId, req.getVas());

        // 5. 盆底视诊（始终创建）
        savePelvicInspection(examinationId, req.getInspection());

        // 6. 特殊检查（始终创建，11 项 +/-）
        saveSpecialExam(examinationId, req.getSpecialTest());

        // 7. 神经损伤查体（始终创建）
        saveNeurologicalExam(examinationId, req.getNerve());

        // 8. 肛门触诊（始终创建）
        saveAnalPalpation(examinationId, req.getPalpation());

        // 9. 其它补充 → 临床记录
        if (hasText(req.getSupplement())) {
            ClinicalNote note = new ClinicalNote();
            note.setPatientId(patient.getId());
            note.setExaminationId(examinationId);
            note.setDoctorId(doctorId);
            note.setNoteType("阶段评估");
            note.setContent(req.getSupplement().trim());
            note.setVisibility("医务人员");
            clinicalNoteMapper.insert(note);
        }
    }

    private void savePostpartum(Long examinationId, SupplementRequest.Postpartum form) {
        SupplementRequest.Postpartum p = form == null ? new SupplementRequest.Postpartum() : form;
        Postpartum postpartum = new Postpartum();
        postpartum.setExaminationId(examinationId);
        postpartum.setUrine(normalizeUrine(p.getUrine()));
        postpartum.setStool(normalizeStool(p.getStool()));
        postpartum.setPelvicPerinealPainVas(parseIntOrDefault(p.getPelvicVas(), 0));
        postpartum.setOrganProlapse(blankToNull(p.getProlapse()));
        postpartum.setScar("有".equals(trim(p.getScar())));
        postpartum.setScarTexture(blankToNull(p.getScarTexture()));
        postpartum.setScarColor(blankToNull(p.getScarColor()));
        postpartum.setScarLengthCm(parseDecimalOrNull(p.getScarLength()));
        postpartum.setScarDate(parseDateOrNull(p.getScarDate()));
        postpartum.setUrethralInstability(hasText(p.getUrethra()) && !"无".equals(trim(p.getUrethra())));
        postpartum.setOther(blankToNull(p.getOther()));
        postpartumMapper.insert(postpartum);
    }

    private void savePainAssessment(Long examinationId, Map<String, Integer> vas) {
        PainAssessment pain = new PainAssessment();
        pain.setExaminationId(examinationId);
        pain.setBackPainLeftVas(vasValue(vas, "back左"));
        pain.setBackPainRightVas(vasValue(vas, "back右"));
        pain.setBackPainCenterVas(vasValue(vas, "back中"));
        pain.setSiJointPainLeftVas(vasValue(vas, "si左"));
        pain.setSiJointPainRightVas(vasValue(vas, "si右"));
        pain.setSiJointPainCenterVas(vasValue(vas, "si中"));
        pain.setPubicPainVas(vasValue(vas, "pubisVAS"));
        pain.setCoccyxPainLeftVas(vasValue(vas, "coccyx左"));
        pain.setCoccyxPainRightVas(vasValue(vas, "coccyx右"));
        pain.setCoccyxPainCenterVas(vasValue(vas, "coccyx中"));
        pain.setSciaticPainLeftVas(vasValue(vas, "sciatic左"));
        pain.setSciaticPainRightVas(vasValue(vas, "sciatic右"));
        pain.setGroinPainLeftVas(vasValue(vas, "groin左"));
        pain.setGroinPainRightVas(vasValue(vas, "groin右"));
        pain.setNeckPainLeftVas(vasValue(vas, "neck左"));
        pain.setNeckPainRightVas(vasValue(vas, "neck右"));
        pain.setArmPainLeftVas(vasValue(vas, "arm左"));
        pain.setArmPainRightVas(vasValue(vas, "arm右"));
        pain.setLegPainLeftVas(vasValue(vas, "leg左"));
        pain.setLegPainRightVas(vasValue(vas, "leg右"));
        painAssessmentMapper.insert(pain);
    }

    private void savePelvicInspection(Long examinationId, Map<String, String> inspection) {
        Map<String, String> map = inspection == null ? Map.of() : inspection;
        PelvicInspection pelvic = new PelvicInspection();
        pelvic.setExaminationId(examinationId);
        pelvic.setVulva(text(map, "vulva"));
        pelvic.setVaginalTear(text(map, "vaginalTear"));
        pelvic.setUrethralMeatus(text(map, "urethral"));
        pelvic.setClitoris(text(map, "clitoris"));
        pelvic.setLabiaSymmetry("是".equals(trim(map.get("labiaSym"))));
        pelvic.setDischarge("有".equals(trim(map.get("discharge"))));
        pelvic.setDischargeColor(blankToNull(map.get("dischargeColor")));
        pelvic.setDischargeSmell(blankToNull(map.get("dischargeSmell")));
        pelvic.setDischargeTexture(blankToNull(map.get("dischargeTexture")));
        pelvic.setDischargeAmount(parseDecimalOrNull(map.get("dischargeAmount")));
        pelvic.setPubicHair(text(map, "pubicHair"));
        pelvic.setHemorrhoids(text(map, "hemorrhoids"));
        pelvic.setValsalva(text(map, "valsalva"));
        pelvicInspectionMapper.insert(pelvic);
    }

    private void saveSpecialExam(Long examinationId, Map<String, String> specialTest) {
        Map<String, String> map = specialTest == null ? Map.of() : specialTest;
        SpecialExam special = new SpecialExam();
        special.setExaminationId(examinationId);
        special.setUrogenitalTest(sign(map.get("urogenital")));
        special.setScarTest(sign(map.get("scarTest")));
        special.setFaberLeft(sign(map.get("faberL")));
        special.setFaberRight(sign(map.get("faberR")));
        special.setPelvicSeparation(sign(map.get("pelvicSep")));
        special.setPelvicCompression(sign(map.get("pelvicComp")));
        special.setSacralCompression(sign(map.get("sacralPress")));
        special.setFemoralImpaction(sign(map.get("femoralImp")));
        special.setStraightLegRaise(sign(map.get("slr")));
        special.setStraightLegRaiseReinforced(sign(map.get("slrReinforced")));
        special.setPalpationTest(sign(map.get("palpationTest")));
        special.setOther(blankToNull(map.get("other")));
        specialExamMapper.insert(special);
    }

    private void saveNeurologicalExam(Long examinationId, Map<String, String> nerve) {
        Map<String, String> map = nerve == null ? Map.of() : nerve;
        NeurologicalExam exam = new NeurologicalExam();
        exam.setExaminationId(examinationId);
        exam.setPelvicFloorTone(enumValue(map.get("pfTone"), PELVIC_TONE_VALUES, "正常"));
        exam.setLeftSensation(enumValue(map.get("sensation左"), SENSATION_VALUES, "正常"));
        exam.setRightSensation(enumValue(map.get("sensation右"), SENSATION_VALUES, "正常"));
        exam.setCatheter("有".equals(trim(map.get("catheter"))));
        neurologicalExamMapper.insert(exam);
    }

    private void saveAnalPalpation(Long examinationId, Map<String, String> palpation) {
        Map<String, String> map = palpation == null ? Map.of() : palpation;
        AnalPalpation anal = new AnalPalpation();
        anal.setExaminationId(examinationId);
        anal.setLevatorAni(text(map, "levatorAni"));
        anal.setCoccygeus(text(map, "coccygeus"));
        anal.setIliococcygeus(text(map, "iliococcygeus"));
        anal.setObturatorInternus(text(map, "obturatorInternus"));
        anal.setOrganShift(text(map, "organShift"));
        anal.setOxfordGrade(parseIntOrNull(map.get("oxford")));
        analPalpationMapper.insert(anal);
    }

    /** 药物治疗：多选拼接“、”，勾选其他且填写时追加 */
    private String buildMedication(SupplementRequest req) {
        StringBuilder sb = new StringBuilder();
        List<String> medication = req.getMedication();
        if (medication != null) {
            for (String item : medication) {
                if (!hasText(item)) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append("、");
                }
                sb.append(item.trim());
            }
        }
        if (Boolean.TRUE.equals(req.getMedOtherChecked()) && hasText(req.getMedOther())) {
            if (sb.length() > 0) {
                sb.append("、");
            }
            sb.append(req.getMedOther().trim());
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /** 前端枚举值归一：压力/急迫/充溢/混合 → 数据库 ENUM 值 */
    private String normalizeUrine(String value) {
        String v = trim(value);
        if (v == null || v.isEmpty()) {
            return "无";
        }
        switch (v) {
            case "压力":
                return "压力性";
            case "急迫":
                return "急迫性";
            case "充溢":
                return "充溢性";
            case "混合":
                return "混合性";
            default:
                return URINE_VALUES.contains(v) ? v : "无";
        }
    }

    private String normalizeStool(String value) {
        String v = trim(value);
        return v != null && STOOL_VALUES.contains(v) ? v : "正常";
    }

    private String enumValue(String value, Set<String> allowed, String defaultValue) {
        String v = trim(value);
        return v != null && allowed.contains(v) ? v : defaultValue;
    }

    /** 特殊检查统一为 +/-，缺省 - */
    private String sign(String value) {
        return "+".equals(trim(value)) ? "+" : "-";
    }

    private int vasValue(Map<String, Integer> vas, String key) {
        Integer value = vas == null ? null : vas.get(key);
        return value == null ? 0 : value;
    }

    private BigDecimal decimalOrZero(Map<String, String> map, String key) {
        BigDecimal value = parseDecimalOrNull(map == null ? null : map.get(key));
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal parseDecimalOrNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntOrNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        Integer parsed = parseIntOrNull(value);
        return parsed == null ? defaultValue : parsed;
    }

    private LocalDate parseDateOrNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        String v = value.trim();
        try {
            return LocalDate.parse(v.length() > 10 ? v.substring(0, 10) : v);
        } catch (Exception e) {
            return null;
        }
    }

    private String text(Map<String, String> map, String key) {
        String value = map == null ? null : map.get(key);
        return value == null ? "" : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
