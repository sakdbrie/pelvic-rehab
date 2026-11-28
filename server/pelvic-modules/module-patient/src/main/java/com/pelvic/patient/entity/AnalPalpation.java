package com.pelvic.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 肛门触诊 */
@Data
@TableName("anal_palpation")
public class AnalPalpation {

    @TableId(value = "触诊ID", type = IdType.AUTO)
    private Long id;

    @TableField("检查ID")
    private Long examinationId;

    @TableField("肛提肌")
    private String levatorAni;

    @TableField("坐尾肌")
    private String coccygeus;

    @TableField("髂尾肌")
    private String iliococcygeus;

    @TableField("闭孔内肌")
    private String obturatorInternus;

    @TableField("脏器偏移")
    private String organShift;

    /** 牛津肌力（级） */
    @TableField("牛津肌力")
    private Integer oxfordGrade;

    /** 尾骨嵌顿（1=有） */
    @TableField("尾骨嵌顿")
    private Boolean coccyxImpaction;

    @TableField("肛周温度")
    private String perianalTemperature;

    @TableField("肛周湿度")
    private String perianalMoisture;

    @TableField("会阴及肛周肌肉张力")
    private String perinealMuscleTone;
}
