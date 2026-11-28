package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 患者健康档案（与患者一对一，仅保留最新资料） */
@Data
@TableName("patient_health_profile")
public class PatientHealthProfile {

    /** 健康档案ID（PK/FK，等于患者ID，一对一） */
    @TableId(value = "健康档案ID", type = IdType.AUTO)
    private Long id;

    @TableField("患者ID")
    private Long patientId;

    @TableField("主诉")
    private String chief;

    /** 正常 / 便秘 / 大便失禁 */
    @TableField("大便情况")
    private String stool;

    /** 正常 / 尿痛 / 尿失禁 / 尿频尿急 / 尿潴留 */
    @TableField("小便情况")
    private String urine;

    @TableField("性生活频率次每周")
    private BigDecimal sexFrequencyWeekly;

    @TableField("性生活频率次每月")
    private BigDecimal sexFrequencyMonthly;

    /** 冶游史（1=有） */
    @TableField("冶游史")
    private Boolean promiscuityHistory;

    @TableField("既往运动及生活补充")
    private String pastExerciseLifeSupplement;

    /** 是否绝经（1=是） */
    @TableField("是否绝经")
    private Boolean menopause;

    @TableField("月经持续天数")
    private Integer menstruationDays;

    @TableField("月经周期天数")
    private Integer cycleDays;

    @TableField("痛经VAS")
    private Integer dysmenorrheaVas;

    @TableField("末次月经日期")
    private LocalDate lastMenstrualDate;

    @TableField("绝经年龄")
    private Integer menopauseAge;

    @TableField("吸烟支每天")
    private BigDecimal smokingPerDay;

    @TableField("饮酒ml每天")
    private BigDecimal alcoholMlPerDay;

    @TableField("茶咖啡ml每天")
    private BigDecimal teaCoffeeMlPerDay;

    @TableField("久坐小时每天")
    private BigDecimal sedentaryHoursPerDay;

    @TableField("久站小时每天")
    private BigDecimal standingHoursPerDay;

    @TableField("其他习惯")
    private String otherHabits;

    /** 逻辑删除（1=已删除） */
    @TableField("逻辑删除")
    private Integer deleted;

    @TableField(value = "创建时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(value = "更新时间", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
