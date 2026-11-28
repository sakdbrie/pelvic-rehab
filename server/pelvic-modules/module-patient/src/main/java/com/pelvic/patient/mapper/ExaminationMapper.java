package com.pelvic.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.patient.entity.Examination;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExaminationMapper extends BaseMapper<Examination> {

    /** 患者最近一次临床检查（详情页只读视图） */
    @Select("SELECT 检查ID AS id, 患者ID AS patientId, 录入医生ID AS doctorId, "
            + "最后修改医生ID AS lastModifyDoctorId, 检查时间 AS examTime, 主诉 AS chief, "
            + "腹部张力 AS abdominalTension, 诊断 AS diagnosis, 药物治疗 AS medication, 其他补充 AS otherSupplement, "
            + "创建时间 AS createTime, 更新时间 AS updateTime "
            + "FROM examination WHERE 患者ID = #{patientId} ORDER BY 检查时间 DESC, 检查ID DESC LIMIT 1")
    Examination selectLatestByPatient(Long patientId);
}
