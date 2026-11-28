package com.pelvic.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.patient.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

    /** 当前登录账号对应的医生档案ID（补充信息需记录录入医生） */
    @Select("SELECT 医生ID FROM doctor WHERE 账号ID = #{accountId} LIMIT 1")
    Long selectDoctorId(Long accountId);

    /** 患者账号手机号密文（详情页脱敏展示） */
    @Select("SELECT 手机号密文 FROM account WHERE 账号ID = #{accountId}")
    String selectPhoneCipherByAccountId(Long accountId);
}
