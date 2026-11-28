package com.pelvic.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.report.entity.QuestionnaireSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 量表填写记录。
 * 自定义查询返回实体时必须把中文列别名成 Java 属性名（MyBatis 原始 @Select 不走 @TableField 映射）。
 */
@Mapper
public interface QuestionnaireSubmissionMapper extends BaseMapper<QuestionnaireSubmission> {

    String COLUMNS = "填写记录ID AS id, 患者ID AS patientId, 量表ID AS questionnaireId, "
            + "填写时间 AS fillTime, 填写状态 AS fillStatus, 总分 AS totalScore, "
            + "维度得分快照 AS dimensionScores, 评估结果 AS result, 提交时间 AS submitTime, "
            + "医生点评 AS doctorComment, 点评医生ID AS commentDoctorId, 点评时间 AS commentTime, "
            + "创建时间 AS createTime, 更新时间 AS updateTime";

    @Select("SELECT " + COLUMNS + " FROM questionnaire_submission WHERE 患者ID = #{patientId} "
            + "AND 填写状态 = '已提交' AND DATE(提交时间) = #{date} ORDER BY 提交时间 DESC LIMIT 1")
    QuestionnaireSubmission selectLatestByPatientAndDate(@Param("patientId") Long patientId,
                                                         @Param("date") String date);

    @Select("SELECT " + COLUMNS + " FROM questionnaire_submission WHERE 患者ID = #{patientId} "
            + "AND 量表ID = #{qid} AND 填写状态 = '已提交' AND 提交时间 IS NOT NULL "
            + "ORDER BY 提交时间 DESC LIMIT 5")
    List<QuestionnaireSubmission> selectHistory(@Param("patientId") Long patientId, @Param("qid") Long qid);
}
