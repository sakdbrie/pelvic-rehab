package com.pelvic.patient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.core.util.AesEncryptor;
import com.pelvic.patient.dto.PatientDetailResponse;
import com.pelvic.patient.entity.AnalPalpation;
import com.pelvic.patient.entity.Diastasis;
import com.pelvic.patient.entity.Examination;
import com.pelvic.patient.entity.NeurologicalExam;
import com.pelvic.patient.entity.PainAssessment;
import com.pelvic.patient.entity.Patient;
import com.pelvic.patient.entity.PatientConditions;
import com.pelvic.patient.entity.PatientHealthProfile;
import com.pelvic.patient.entity.PelvicInspection;
import com.pelvic.patient.entity.Postpartum;
import com.pelvic.patient.entity.Pregnancy;
import com.pelvic.patient.entity.SpecialExam;
import com.pelvic.patient.entity.SurgeryHistory;
import com.pelvic.patient.mapper.AnalPalpationMapper;
import com.pelvic.patient.mapper.ClinicalNoteMapper;
import com.pelvic.patient.mapper.DiastasisMapper;
import com.pelvic.patient.mapper.ExaminationMapper;
import com.pelvic.patient.mapper.NeurologicalExamMapper;
import com.pelvic.patient.mapper.PainAssessmentMapper;
import com.pelvic.patient.mapper.PatientConditionsMapper;
import com.pelvic.patient.mapper.PatientHealthProfileMapper;
import com.pelvic.patient.mapper.PatientMapper;
import com.pelvic.patient.mapper.PelvicInspectionMapper;
import com.pelvic.patient.mapper.PostpartumMapper;
import com.pelvic.patient.mapper.PregnancyMapper;
import com.pelvic.patient.mapper.SpecialExamMapper;
import com.pelvic.patient.mapper.SurgeryHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/** 患者详情聚合查询（后端实现逻辑.md 第 9 节） */
@Slf4j
@Service
public class PatientQueryService {

    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private PatientHealthProfileMapper healthProfileMapper;
    @Autowired
    private PatientConditionsMapper conditionsMapper;
    @Autowired
    private SurgeryHistoryMapper surgeryHistoryMapper;
    @Autowired
    private PregnancyMapper pregnancyMapper;
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
    @Autowired
    private AesEncryptor aesEncryptor;

    /**
     * 患者详情：基本档案 + 健康档案 + 既往史/手术史 + 妊娠记录 + 最近一次临床检查及子表。
     *
     * @param patientId 患者编号（如 P20260001），不是数据库主键
     */
    public PatientDetailResponse getDetail(String patientId) {
        // TODO 数据范围校验：医生仅可查看授权范围内患者（授权模式后续实现）
        Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getPatientNo, patientId)
                .eq(Patient::getDeleted, 0)
                .last("LIMIT 1"));
        if (patient == null) {
            throw new BizException("患者不存在");
        }

        PatientDetailResponse resp = new PatientDetailResponse();
        resp.setPatientId(patient.getPatientNo());
        resp.setName(patient.getName());
        resp.setGender(patient.getGender());
        resp.setBirthDate(patient.getBirthDate());
        resp.setAge(patient.getBirthDate() == null
                ? null : LocalDate.now().getYear() - patient.getBirthDate().getYear());
        resp.setPhone(maskPhone(patient.getAccountId()));
        resp.setHeightCm(patient.getHeightCm());
        resp.setWeightKg(patient.getWeightKg());
        resp.setMarriage(patient.getMarriage());
        resp.setEducation(patient.getEducation());
        resp.setJob(patient.getJob());
        resp.setRemark(patient.getRemark());

        resp.setHealthProfile(healthProfileMapper.selectOne(new LambdaQueryWrapper<PatientHealthProfile>()
                .eq(PatientHealthProfile::getPatientId, patient.getId())
                .eq(PatientHealthProfile::getDeleted, 0)
                .last("LIMIT 1")));
        resp.setConditions(conditionsMapper.selectList(new LambdaQueryWrapper<PatientConditions>()
                .eq(PatientConditions::getPatientId, patient.getId())));
        resp.setSurgeries(surgeryHistoryMapper.selectList(new LambdaQueryWrapper<SurgeryHistory>()
                .eq(SurgeryHistory::getPatientId, patient.getId())
                .eq(SurgeryHistory::getDeleted, 0)));
        resp.setPregnancies(pregnancyMapper.selectList(new LambdaQueryWrapper<Pregnancy>()
                .eq(Pregnancy::getPatientId, patient.getId())
                .eq(Pregnancy::getDeleted, 0)
                .orderByAsc(Pregnancy::getPregnancyNo)));

        Examination examination = examinationMapper.selectLatestByPatient(patient.getId());
        if (examination != null) {
            PatientDetailResponse.LatestExam latest = new PatientDetailResponse.LatestExam();
            latest.setExamination(examination);
            Long examinationId = examination.getId();
            latest.setDiastasis(diastasisMapper.selectOne(new LambdaQueryWrapper<Diastasis>()
                    .eq(Diastasis::getExaminationId, examinationId)
                    .last("LIMIT 1")));
            latest.setPostpartum(postpartumMapper.selectOne(new LambdaQueryWrapper<Postpartum>()
                    .eq(Postpartum::getExaminationId, examinationId)
                    .last("LIMIT 1")));
            latest.setPainAssessment(painAssessmentMapper.selectOne(new LambdaQueryWrapper<PainAssessment>()
                    .eq(PainAssessment::getExaminationId, examinationId)
                    .last("LIMIT 1")));
            latest.setPelvicInspection(pelvicInspectionMapper.selectOne(new LambdaQueryWrapper<PelvicInspection>()
                    .eq(PelvicInspection::getExaminationId, examinationId)
                    .last("LIMIT 1")));
            latest.setSpecialExam(specialExamMapper.selectOne(new LambdaQueryWrapper<SpecialExam>()
                    .eq(SpecialExam::getExaminationId, examinationId)
                    .last("LIMIT 1")));
            latest.setNeurologicalExam(neurologicalExamMapper.selectOne(new LambdaQueryWrapper<NeurologicalExam>()
                    .eq(NeurologicalExam::getExaminationId, examinationId)
                    .last("LIMIT 1")));
            latest.setAnalPalpation(analPalpationMapper.selectOne(new LambdaQueryWrapper<AnalPalpation>()
                    .eq(AnalPalpation::getExaminationId, examinationId)
                    .last("LIMIT 1")));
            // 临床补充记录按患者全量返回（不局限于本次检查）
            latest.setNotes(clinicalNoteMapper.selectByPatient(patient.getId()));
            resp.setLatestExam(latest);
        }
        return resp;
    }

    /** 手机号密文解密并脱敏：138****0000；解密失败返回 **** */
    private String maskPhone(Long accountId) {
        if (accountId == null) {
            return "****";
        }
        try {
            String phone = aesEncryptor.decrypt(patientMapper.selectPhoneCipherByAccountId(accountId));
            if (phone != null && phone.length() == 11) {
                return phone.substring(0, 3) + "****" + phone.substring(7);
            }
        } catch (Exception e) {
            log.warn("手机号解密失败（请检查 pelvic.encrypt.aes-key 与存量数据密钥一致性）");
        }
        return "****";
    }
}
