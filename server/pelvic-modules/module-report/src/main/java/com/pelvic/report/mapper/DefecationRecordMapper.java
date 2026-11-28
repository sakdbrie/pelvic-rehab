package com.pelvic.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.report.entity.DefecationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 排便记录 */
@Mapper
public interface DefecationRecordMapper extends BaseMapper<DefecationRecord> {

    @Select("SELECT COUNT(*) FROM defecation_record WHERE 患者ID = #{patientId} AND DATE(发生时间) = #{date}")
    int countByPatientAndDate(@Param("patientId") Long patientId, @Param("date") String date);
}
