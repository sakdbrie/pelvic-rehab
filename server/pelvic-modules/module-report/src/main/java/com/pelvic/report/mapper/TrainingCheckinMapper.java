package com.pelvic.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.report.entity.TrainingCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 训练打卡 */
@Mapper
public interface TrainingCheckinMapper extends BaseMapper<TrainingCheckin> {

    String COLUMNS = "打卡ID AS id, 患者ID AS patientId, 内容ID AS contentId, "
            + "训练日程ID AS trainingDayId, 开始播放时间 AS startPlayTime, 完成时间 AS completeTime, "
            + "播放进度百分比 AS progressPercent, 打卡状态 AS checkinStatus";

    @Select("SELECT COUNT(*) FROM training_checkin WHERE 患者ID = #{patientId} "
            + "AND DATE(开始播放时间) = #{date}")
    int countStartedByPatientAndDate(@Param("patientId") Long patientId, @Param("date") String date);

    @Select("SELECT COUNT(*) FROM training_checkin WHERE 患者ID = #{patientId} "
            + "AND 打卡状态 = '已完成' AND 完成时间 IS NOT NULL AND DATE(开始播放时间) = #{date}")
    int countDoneByPatientAndDate(@Param("patientId") Long patientId, @Param("date") String date);

    @Select("SELECT " + COLUMNS + " FROM training_checkin WHERE 患者ID = #{patientId} "
            + "AND DATE(开始播放时间) = #{date} ORDER BY 开始播放时间")
    List<TrainingCheckin> selectByPatientAndDate(@Param("patientId") Long patientId, @Param("date") String date);

    @Select("SELECT COUNT(DISTINCT 患者ID) FROM training_checkin WHERE DATE(开始播放时间) = CURDATE()")
    long countTodayStartedPatients();

    @Select("SELECT COUNT(DISTINCT 患者ID) FROM training_checkin "
            + "WHERE DATE(开始播放时间) = CURDATE() AND 打卡状态 = '已完成'")
    long countTodayDonePatients();
}
