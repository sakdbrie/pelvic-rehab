package com.pelvic.patient.dto;

import com.pelvic.patient.entity.AnalPalpation;
import com.pelvic.patient.entity.ClinicalNote;
import com.pelvic.patient.entity.Diastasis;
import com.pelvic.patient.entity.Examination;
import com.pelvic.patient.entity.NeurologicalExam;
import com.pelvic.patient.entity.PainAssessment;
import com.pelvic.patient.entity.PatientConditions;
import com.pelvic.patient.entity.PatientHealthProfile;
import com.pelvic.patient.entity.PelvicInspection;
import com.pelvic.patient.entity.Postpartum;
import com.pelvic.patient.entity.Pregnancy;
import com.pelvic.patient.entity.SpecialExam;
import com.pelvic.patient.entity.SurgeryHistory;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 患者详情响应（后端实现逻辑.md 第 9.4、9.5 节：基本档案 + 病史 + 孕产 + 最近一次临床检查） */
@Data
public class PatientDetailResponse {

    /** 患者编号（与列表页/路由参数一致，如 P20260001） */
    private String patientId;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private Integer age;
    /** 已脱敏手机号（138****0000），解密失败返回 **** */
    private String phone;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String marriage;
    private String education;
    private String job;
    private String remark;

    private PatientHealthProfile healthProfile;
    private List<PatientConditions> conditions;
    private List<SurgeryHistory> surgeries;
    private List<Pregnancy> pregnancies;

    private LatestExam latestExam;

    /** 最近一次临床检查：主记录 + 已执行检查子表 + 患者全部临床补充记录 */
    @Data
    public static class LatestExam {
        private Examination examination;
        private Diastasis diastasis;
        private Postpartum postpartum;
        private PainAssessment painAssessment;
        private PelvicInspection pelvicInspection;
        private SpecialExam specialExam;
        private NeurologicalExam neurologicalExam;
        private AnalPalpation analPalpation;
        private List<ClinicalNote> notes;
    }
}
