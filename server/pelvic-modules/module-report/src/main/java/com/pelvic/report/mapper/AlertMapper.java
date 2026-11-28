package com.pelvic.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.report.entity.Alert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 异常预警 */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {

    @Select("SELECT COUNT(DISTINCT 患者ID) FROM alert WHERE 处理状态 IN ('待处理','处理中') "
            + "AND DATE(预警时间) BETWEEN #{start} AND #{end}")
    long countPatientsWithAlert(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM alert WHERE 患者ID = #{patientId} "
            + "AND 处理状态 IN ('待处理','处理中') AND DATE(预警时间) = #{date}")
    int countByPatientAndDate(@Param("patientId") Long patientId, @Param("date") String date);
}
