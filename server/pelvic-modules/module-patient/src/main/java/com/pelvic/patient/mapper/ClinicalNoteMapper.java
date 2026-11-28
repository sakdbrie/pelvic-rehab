package com.pelvic.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.patient.entity.ClinicalNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClinicalNoteMapper extends BaseMapper<ClinicalNote> {

    /** 患者全部临床补充记录（按创建时间倒序） */
    @Select("SELECT 临床记录ID AS id, 患者ID AS patientId, 检查ID AS examinationId, 医生ID AS doctorId, "
            + "记录类型 AS noteType, 记录内容 AS content, 可见范围 AS visibility, "
            + "创建时间 AS createTime, 更新时间 AS updateTime "
            + "FROM clinical_note WHERE 患者ID = #{patientId} ORDER BY 创建时间 DESC")
    List<ClinicalNote> selectByPatient(Long patientId);
}
