package com.pelvic.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.report.entity.UrinationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 排尿记录（概览/详情聚合口径：排尿=自主排尿+漏尿+间歇导尿） */
@Mapper
public interface UrinationRecordMapper extends BaseMapper<UrinationRecord> {

    @Select("SELECT COUNT(*) FROM urination_record WHERE 患者ID = #{patientId} "
            + "AND 记录类型 IN ('自主排尿','漏尿','间歇导尿') AND DATE(发生时间) = #{date}")
    int countVoidByPatientAndDate(@Param("patientId") Long patientId, @Param("date") String date);

    @Select("SELECT IFNULL(SUM(饮水量ml), 0) FROM urination_record WHERE 患者ID = #{patientId} "
            + "AND 记录类型 = '饮水' AND DATE(发生时间) = #{date}")
    int sumWaterByPatientAndDate(@Param("patientId") Long patientId, @Param("date") String date);

    @Select("SELECT COUNT(*) FROM urination_record WHERE 患者ID = #{patientId} "
            + "AND 记录类型 IN ('漏尿','间歇导尿') AND DATE(发生时间) = #{date}")
    int countAbnormalByPatientAndDate(@Param("patientId") Long patientId, @Param("date") String date);
}
