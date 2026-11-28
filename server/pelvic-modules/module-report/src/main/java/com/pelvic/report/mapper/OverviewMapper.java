package com.pelvic.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 数据总览聚合（后端实现逻辑.md 第 14、15、17 节）。
 * 以“患者 + 日期”为行，只统计有排尿记录（自主排尿/漏尿/间歇导尿）的日期。
 * 先在派生表 t 中按患者+日期分组，再对 t 做相关子查询与筛选
 * （避免相关子查询引用未分组列触发 only_full_group_by 报错）。
 * alert / done 布尔与 KPI 由 Service 补充（v1 允许 N+1，TODO 优化为单条聚合 SQL）。
 */
@Mapper
public interface OverviewMapper {

    /** 派生表：患者+日期（含排尿类记录的日子） */
    String BASE = """
            SELECT p.患者ID AS patientId, p.患者编号 AS id, p.姓名 AS name, DATE(u.发生时间) AS date
            FROM patient p
            JOIN urination_record u ON u.患者ID = p.患者ID
                 AND u.记录类型 IN ('自主排尿','漏尿','间歇导尿')
            WHERE p.是否删除 = 0
              AND DATE(u.发生时间) BETWEEN #{startDate} AND #{endDate}
            GROUP BY p.患者ID, p.患者编号, p.姓名, DATE(u.发生时间)
            """;

    /** 对派生表 t 的动态筛选条件 */
    String CONDITIONS = """
             WHERE 1=1
            <if test='dataStatus == "alert"'>
              AND EXISTS (SELECT 1 FROM alert a WHERE a.患者ID = t.patientId
                          AND a.处理状态 IN ('待处理','处理中') AND DATE(a.预警时间) = t.date)
            </if>
            <if test='dataStatus == "normal"'>
              AND NOT EXISTS (SELECT 1 FROM alert a WHERE a.患者ID = t.patientId
                              AND a.处理状态 IN ('待处理','处理中') AND DATE(a.预警时间) = t.date)
            </if>
            <if test='checkStatus == "done"'>
              AND EXISTS (SELECT 1 FROM training_checkin tc WHERE tc.患者ID = t.patientId
                          AND tc.打卡状态 = '已完成' AND DATE(tc.开始播放时间) = t.date)
            </if>
            <if test='checkStatus == "undone"'>
              AND NOT EXISTS (SELECT 1 FROM training_checkin tc WHERE tc.患者ID = t.patientId
                              AND tc.打卡状态 = '已完成' AND DATE(tc.开始播放时间) = t.date)
            </if>
            <if test='keyword != null'>
              AND (t.id LIKE CONCAT('%', #{keyword}, '%') OR t.name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            """;

    @Select("""
            <script>
            SELECT t.patientId, t.id, t.name, t.date,
                   (SELECT COUNT(*) FROM urination_record u2 WHERE u2.患者ID = t.patientId
                     AND DATE(u2.发生时间) = t.date
                     AND u2.记录类型 IN ('自主排尿','漏尿','间歇导尿')) AS urine,
                   (SELECT COUNT(*) FROM defecation_record d2 WHERE d2.患者ID = t.patientId
                     AND DATE(d2.发生时间) = t.date) AS stool,
                   (SELECT IFNULL(SUM(u3.饮水量ml), 0) FROM urination_record u3 WHERE u3.患者ID = t.patientId
                     AND DATE(u3.发生时间) = t.date AND u3.记录类型 = '饮水') AS water
            FROM (
            """ + BASE + """
            ) t
            """ + CONDITIONS + """
            ORDER BY t.date DESC, t.id
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<Map<String, Object>> selectRows(@Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("dataStatus") String dataStatus,
                                         @Param("checkStatus") String checkStatus,
                                         @Param("keyword") String keyword,
                                         @Param("offset") long offset,
                                         @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM (
            """ + BASE + """
            ) t
            """ + CONDITIONS + """
            </script>
            """)
    long countRows(@Param("startDate") String startDate,
                   @Param("endDate") String endDate,
                   @Param("dataStatus") String dataStatus,
                   @Param("checkStatus") String checkStatus,
                   @Param("keyword") String keyword);
}
