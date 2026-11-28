package com.pelvic.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.report.entity.Questionnaire;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/** 自评量表定义 */
@Mapper
public interface QuestionnaireMapper extends BaseMapper<Questionnaire> {

    @Select("SELECT 量表名称 FROM questionnaire WHERE 量表ID = #{id}")
    String selectNameById(@Param("id") Long id);

    /** 按量表名称取 ID/编码（v1 量表未 seed 时返回 null，由服务层优雅降级） */
    @Select("SELECT 量表ID AS id, 量表编码 AS code FROM questionnaire "
            + "WHERE 量表名称 = #{name} AND 逻辑删除 = 0 LIMIT 1")
    Map<String, Object> selectRefByName(@Param("name") String name);
}
