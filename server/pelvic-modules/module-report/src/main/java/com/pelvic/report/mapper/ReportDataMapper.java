package com.pelvic.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 七张报表的数据查询（后端实现逻辑.md 第 24 节）。
 * female / male 的患者基础数据 + 明细表以 Java 组装（本接口提供分页患者与各明细行）；
 * 其余报表为单条聚合 SQL。
 */
@Mapper
public interface ReportDataMapper {

    // ==================== female / male 患者分页 ====================

    @Select("""
            <script>
            SELECT p.患者ID AS patientId, p.患者编号 AS id, p.姓名 AS name, p.性别 AS gender,
                   p.出生年月 AS birth, p.身高cm AS height, p.体重kg AS weight,
                   p.婚姻状况 AS marriage, p.教育程度 AS education, p.从事职业 AS job,
                   a.手机号密文 AS phoneCipher
            FROM patient p
            LEFT JOIN account a ON a.账号ID = p.账号ID
            WHERE p.是否删除 = 0 AND p.性别 = #{gender}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%') OR p.姓名 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY p.患者编号
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectPatients(@Param("gender") String gender,
                                             @Param("keyword") String keyword,
                                             @Param("offset") long offset,
                                             @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM patient p
            WHERE p.是否删除 = 0 AND p.性别 = #{gender}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%') OR p.姓名 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countPatients(@Param("gender") String gender, @Param("keyword") String keyword);

    // ==================== 基础信息/病史/检查明细（返回中文列名，Service 直接按中文键取值） ====================

    @Select("SELECT * FROM patient_health_profile WHERE 患者ID = #{patientId} AND 逻辑删除 = 0 LIMIT 1")
    Map<String, Object> selectHealthProfile(@Param("patientId") Long patientId);

    @Select("SELECT * FROM pregnancy WHERE 患者ID = #{patientId} AND 逻辑删除 = 0 "
            + "ORDER BY 妊娠序号 DESC, 创建时间 DESC LIMIT 1")
    Map<String, Object> selectLatestPregnancy(@Param("patientId") Long patientId);

    @Select("SELECT COUNT(*) AS total, "
            + "IFNULL(SUM(CASE WHEN 分娩日期 IS NOT NULL THEN 1 ELSE 0 END), 0) AS births, "
            + "IFNULL(SUM(CASE WHEN 妊娠结局 LIKE '%流产%' THEN 1 ELSE 0 END), 0) AS abortions "
            + "FROM pregnancy WHERE 患者ID = #{patientId} AND 逻辑删除 = 0")
    Map<String, Object> selectPregnancyCounts(@Param("patientId") Long patientId);

    @Select("SELECT GROUP_CONCAT(CONCAT(IFNULL(手术类别, ''), '：', IFNULL(手术名称, ''), "
            + "IF(手术日期 IS NULL, '', CONCAT('（', DATE_FORMAT(手术日期, '%Y-%m-%d'), '）'))) "
            + "SEPARATOR '；') AS summary "
            + "FROM surgery_history WHERE 患者ID = #{patientId} AND 逻辑删除 = 0")
    String selectSurgerySummary(@Param("patientId") Long patientId);

    @Select("SELECT 项目类别, 项目名称, 详情 FROM patient_conditions WHERE 患者ID = #{patientId} "
            + "ORDER BY 项目类别, 病史项目ID")
    List<Map<String, Object>> selectConditions(@Param("patientId") Long patientId);

    @Select("SELECT * FROM examination WHERE 患者ID = #{patientId} "
            + "ORDER BY 检查时间 DESC, 检查ID DESC LIMIT 1")
    Map<String, Object> selectLatestExamination(@Param("patientId") Long patientId);

    @Select("SELECT * FROM diastasis WHERE 检查ID = #{examId} LIMIT 1")
    Map<String, Object> selectDiastasis(@Param("examId") Long examId);

    @Select("SELECT * FROM postpartum WHERE 检查ID = #{examId} LIMIT 1")
    Map<String, Object> selectPostpartum(@Param("examId") Long examId);

    @Select("SELECT * FROM pain_assessment WHERE 检查ID = #{examId} LIMIT 1")
    Map<String, Object> selectPainAssessment(@Param("examId") Long examId);

    @Select("SELECT * FROM pelvic_inspection WHERE 检查ID = #{examId} LIMIT 1")
    Map<String, Object> selectPelvicInspection(@Param("examId") Long examId);

    @Select("SELECT * FROM general_inspection WHERE 检查ID = #{examId} LIMIT 1")
    Map<String, Object> selectGeneralInspection(@Param("examId") Long examId);

    @Select("SELECT * FROM special_exam WHERE 检查ID = #{examId} LIMIT 1")
    Map<String, Object> selectSpecialExam(@Param("examId") Long examId);

    @Select("SELECT * FROM neurological_exam WHERE 检查ID = #{examId} LIMIT 1")
    Map<String, Object> selectNeurologicalExam(@Param("examId") Long examId);

    @Select("SELECT * FROM anal_palpation WHERE 检查ID = #{examId} LIMIT 1")
    Map<String, Object> selectAnalPalpation(@Param("examId") Long examId);

    @Select("SELECT 记录内容 FROM clinical_note WHERE 患者ID = #{patientId} "
            + "ORDER BY 创建时间 DESC LIMIT 50")
    List<String> selectClinicalNoteContents(@Param("patientId") Long patientId);

    // ==================== urination 排尿日记明细表 ====================

    @Select("""
            <script>
            SELECT p.患者编号 AS id, p.姓名 AS name,
                   DATE_FORMAT(u.发生时间, '%Y-%m-%d') AS date,
                   u.记录类型 AS type,
                   CONCAT(IFNULL(u.饮水量ml, 0), 'ml') AS water,
                   CONCAT(IFNULL(u.自主排尿量ml, 0), 'ml') AS voidVolume,
                   CONCAT(IFNULL(u.漏尿量ml, 0), 'ml') AS leakVolume,
                   CASE WHEN IFNULL(u.导尿量ml, 0) = 0 THEN '—' ELSE CONCAT(u.导尿量ml, 'ml') END AS catheterVolume,
                   IFNULL(NULLIF(TRIM(u.备注), ''), '—') AS other,
                   DATE_FORMAT(u.发生时间, '%Y-%m-%d %H:%i') AS recordTime,
                   (SELECT COUNT(*) FROM urination_record u2 WHERE u2.患者ID = u.患者ID
                     AND DATE(u2.发生时间) = DATE(u.发生时间)
                     AND u2.记录类型 IN ('自主排尿','漏尿','间歇导尿')) AS totalCount
            FROM urination_record u
            JOIN patient p ON p.患者ID = u.患者ID
            WHERE p.是否删除 = 0 AND DATE(u.发生时间) BETWEEN #{startDate} AND #{endDate}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%') OR p.姓名 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY u.发生时间 DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectUrinationRows(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate,
                                                  @Param("keyword") String keyword,
                                                  @Param("offset") long offset,
                                                  @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM urination_record u
            JOIN patient p ON p.患者ID = u.患者ID
            WHERE p.是否删除 = 0 AND DATE(u.发生时间) BETWEEN #{startDate} AND #{endDate}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%') OR p.姓名 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countUrinationRows(@Param("startDate") String startDate,
                            @Param("endDate") String endDate,
                            @Param("keyword") String keyword);

    // ==================== defecation 排便日记明细表 ====================

    @Select("""
            <script>
            SELECT p.患者编号 AS id, p.姓名 AS name,
                   DATE_FORMAT(d.发生时间, '%Y-%m-%d') AS date,
                   d.大便性状 AS stoolType, d.排便速度 AS speed, d.排便量 AS volume,
                   IFNULL(NULLIF(TRIM(d.备注), ''), '—') AS other,
                   DATE_FORMAT(d.发生时间, '%Y-%m-%d %H:%i') AS recordTime
            FROM defecation_record d
            JOIN patient p ON p.患者ID = d.患者ID
            WHERE p.是否删除 = 0 AND DATE(d.发生时间) BETWEEN #{startDate} AND #{endDate}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%') OR p.姓名 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY d.发生时间 DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectDefecationRows(@Param("startDate") String startDate,
                                                   @Param("endDate") String endDate,
                                                   @Param("keyword") String keyword,
                                                   @Param("offset") long offset,
                                                   @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM defecation_record d
            JOIN patient p ON p.患者ID = d.患者ID
            WHERE p.是否删除 = 0 AND DATE(d.发生时间) BETWEEN #{startDate} AND #{endDate}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%') OR p.姓名 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countDefecationRows(@Param("startDate") String startDate,
                             @Param("endDate") String endDate,
                             @Param("keyword") String keyword);

    // ==================== assessment 盆底自评数据表 ====================

    @Select("""
            <script>
            SELECT CONCAT('R', s.填写记录ID) AS recordId,
                   p.患者编号 AS patientId,
                   p.姓名 AS name,
                   q.量表名称 AS scaleName,
                   DATE_FORMAT(COALESCE(s.提交时间, s.填写时间), '%Y-%m-%d %H:%i') AS fillTime,
                   s.总分 AS totalScore,
                   s.维度得分快照 AS dimScoresJson,
                   s.评估结果 AS result
            FROM questionnaire_submission s
            JOIN patient p ON p.患者ID = s.患者ID
            JOIN questionnaire q ON q.量表ID = s.量表ID
            WHERE p.是否删除 = 0 AND s.填写状态 = '已提交'
              AND DATE(COALESCE(s.提交时间, s.填写时间)) BETWEEN #{startDate} AND #{endDate}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%')
                   OR p.姓名 LIKE CONCAT('%', #{keyword}, '%')
                   OR CONCAT('R', s.填写记录ID) LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY COALESCE(s.提交时间, s.填写时间) DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectAssessmentRows(@Param("startDate") String startDate,
                                                   @Param("endDate") String endDate,
                                                   @Param("keyword") String keyword,
                                                   @Param("offset") long offset,
                                                   @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM questionnaire_submission s
            JOIN patient p ON p.患者ID = s.患者ID
            WHERE p.是否删除 = 0 AND s.填写状态 = '已提交'
              AND DATE(COALESCE(s.提交时间, s.填写时间)) BETWEEN #{startDate} AND #{endDate}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%')
                   OR p.姓名 LIKE CONCAT('%', #{keyword}, '%')
                   OR CONCAT('R', s.填写记录ID) LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countAssessmentRows(@Param("startDate") String startDate,
                             @Param("endDate") String endDate,
                             @Param("keyword") String keyword);

    // ==================== training 康复训练打卡数据表 ====================

    @Select("""
            <script>
            SELECT p.患者编号 AS id, c.内容名称 AS title, cat.分类名称 AS type,
                   DATE_FORMAT(t.开始播放时间, '%Y-%m-%d') AS date,
                   c.时长秒 AS durationSeconds
            FROM training_checkin t
            JOIN patient p ON p.患者ID = t.患者ID
            JOIN training_content c ON c.内容ID = t.内容ID
            JOIN training_category cat ON cat.分类ID = c.分类ID
            WHERE p.是否删除 = 0 AND t.打卡状态 = '已完成'
              AND DATE(t.开始播放时间) BETWEEN #{startDate} AND #{endDate}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%')
                   OR p.姓名 LIKE CONCAT('%', #{keyword}, '%')
                   OR c.内容名称 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test='trainingType != null'>
              AND cat.分类名称 = #{trainingType}
            </if>
            ORDER BY t.开始播放时间 DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectTrainingRows(@Param("startDate") String startDate,
                                                 @Param("endDate") String endDate,
                                                 @Param("keyword") String keyword,
                                                 @Param("trainingType") String trainingType,
                                                 @Param("offset") long offset,
                                                 @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM training_checkin t
            JOIN patient p ON p.患者ID = t.患者ID
            JOIN training_content c ON c.内容ID = t.内容ID
            JOIN training_category cat ON cat.分类ID = c.分类ID
            WHERE p.是否删除 = 0 AND t.打卡状态 = '已完成'
              AND DATE(t.开始播放时间) BETWEEN #{startDate} AND #{endDate}
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%')
                   OR p.姓名 LIKE CONCAT('%', #{keyword}, '%')
                   OR c.内容名称 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test='trainingType != null'>
              AND cat.分类名称 = #{trainingType}
            </if>
            </script>
            """)
    long countTrainingRows(@Param("startDate") String startDate,
                           @Param("endDate") String endDate,
                           @Param("keyword") String keyword,
                           @Param("trainingType") String trainingType);

    // ==================== summary 患者数据汇总表 ====================

    @Select("""
            <script>
            SELECT p.患者编号 AS id, p.姓名 AS name,
                   #{startDate} AS statDate,
                   (SELECT q.量表名称 FROM questionnaire_submission s
                      JOIN questionnaire q ON q.量表ID = s.量表ID
                     WHERE s.患者ID = p.患者ID AND s.填写状态 = '已提交' AND s.提交时间 IS NOT NULL
                     ORDER BY s.提交时间 DESC LIMIT 1) AS scaleName,
                   (SELECT s.总分 FROM questionnaire_submission s
                     WHERE s.患者ID = p.患者ID AND s.填写状态 = '已提交' AND s.提交时间 IS NOT NULL
                     ORDER BY s.提交时间 DESC LIMIT 1) AS scaleScore,
                   (SELECT DATE_FORMAT(s.提交时间, '%Y-%m-%d') FROM questionnaire_submission s
                     WHERE s.患者ID = p.患者ID AND s.填写状态 = '已提交' AND s.提交时间 IS NOT NULL
                     ORDER BY s.提交时间 DESC LIMIT 1) AS assessDate,
                   (SELECT CONCAT(ROUND(COUNT(*) / GREATEST(DATEDIFF(#{endDate}, #{startDate}) + 1, 1)), ' 次/日')
                      FROM urination_record ur WHERE ur.患者ID = p.患者ID
                       AND ur.记录类型 IN ('自主排尿','漏尿','间歇导尿')
                       AND DATE(ur.发生时间) BETWEEN #{startDate} AND #{endDate}) AS urineCount,
                   (SELECT CONCAT(ROUND(COUNT(*) / GREATEST(DATEDIFF(#{endDate}, #{startDate}) + 1, 1)), ' 次/日')
                      FROM defecation_record dr WHERE dr.患者ID = p.患者ID
                       AND DATE(dr.发生时间) BETWEEN #{startDate} AND #{endDate}) AS stoolCount,
                   (SELECT CONCAT(COUNT(*), ' 次') FROM urination_record ur2 WHERE ur2.患者ID = p.患者ID
                       AND ur2.记录类型 IN ('漏尿','间歇导尿')
                       AND DATE(ur2.发生时间) BETWEEN #{startDate} AND #{endDate}) AS abnormalCount,
                   (SELECT CASE WHEN COUNT(*) > 0 THEN '已完成' ELSE '未完成' END
                      FROM training_checkin tc WHERE tc.患者ID = p.患者ID AND tc.打卡状态 = '已完成'
                       AND DATE(tc.开始播放时间) BETWEEN #{startDate} AND #{endDate}) AS checkinStatus,
                   (SELECT CONCAT(COUNT(*), ' 次') FROM alert a WHERE a.患者ID = p.患者ID
                       AND DATE(a.预警时间) BETWEEN #{startDate} AND #{endDate}) AS alertCount
            FROM patient p
            WHERE p.是否删除 = 0
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%') OR p.姓名 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY p.患者编号
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectSummaryRows(@Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                @Param("keyword") String keyword,
                                                @Param("offset") long offset,
                                                @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM patient p
            WHERE p.是否删除 = 0
            <if test='keyword != null'>
              AND (p.患者编号 LIKE CONCAT('%', #{keyword}, '%') OR p.姓名 LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countSummaryRows(@Param("keyword") String keyword);
}
