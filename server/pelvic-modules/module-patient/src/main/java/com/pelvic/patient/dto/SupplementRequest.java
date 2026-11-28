package com.pelvic.patient.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 患者详情页“保存补充”请求（结构与 admin-web/src/views/user/PatientDetailView.vue 的 form 对齐）。
 * 所有字段均可空：缺失/空串按各列默认值落库，解析失败不抛出异常。
 */
@Data
public class SupplementRequest {

    /** 主诉 */
    private String chief;

    /** 腹直肌分离：有 / 无 */
    private String drSeparation;

    /** 指宽，键：静息·肚脐、静息·脐上、静息·脐下、收缩·肚脐、收缩·脐上、收缩·脐下 */
    private Map<String, String> drValues;

    /** 产后情况 */
    private Postpartum postpartum;

    /** 疼痛 VAS，键：back左/back右/back中、si左/si右/si中、pubisVAS、coccyx左/coccyx右/coccyx中、sciatic左/sciatic右、groin左/groin右、neck左/neck右、arm左/arm右、leg左/leg右 */
    private Map<String, Integer> vas;

    /** 盆底视诊，键：vulva、vaginalTear、urethral、clitoris、labiaSym、discharge、dischargeColor、dischargeSmell、dischargeTexture、dischargeAmount、pubicHair、hemorrhoids、valsalva、other */
    private Map<String, String> inspection;

    /** 特殊检查（+/-），键：urogenital、scarTest、faberL、faberR、pelvicSep、pelvicComp、sacralPress、femoralImp、slr、slrReinforced、palpationTest、other */
    private Map<String, String> specialTest;

    /** 神经损伤查体，键：pfTone、sensation左、sensation右、catheter */
    private Map<String, String> nerve;

    /** 触诊，键：levatorAni、coccygeus、iliococcygeus、obturatorInternus、organShift、oxford */
    private Map<String, String> palpation;

    /** 诊断 */
    private String diagnosis;

    /** 药物治疗（多选） */
    private List<String> medication;

    /** 药物治疗“其他”是否勾选 */
    private Boolean medOtherChecked;

    /** 药物治疗“其他”内容 */
    private String medOther;

    /** 其它补充 */
    private String supplement;

    /** 产后情况（字段均为前端原样字符串） */
    @Data
    public static class Postpartum {
        private String urine;
        private String stool;
        private String pelvicPain;
        private String pelvicVas;
        private String prolapse;
        private String scar;
        private String scarTexture;
        private String scarColor;
        private String scarLength;
        private String scarDate;
        private String urethra;
        private String other;
    }
}
