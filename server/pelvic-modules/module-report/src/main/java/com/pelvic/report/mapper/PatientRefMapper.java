package com.pelvic.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 患者引用查询（报表路径参数为患者编号，业务查询需内部患者ID） */
@Mapper
public interface PatientRefMapper {

    @Select("SELECT 患者ID FROM patient WHERE 患者编号 = #{no} AND 是否删除 = 0 LIMIT 1")
    Long selectIdByNo(@Param("no") String no);

    @Select("SELECT 姓名 FROM patient WHERE 患者编号 = #{no} AND 是否删除 = 0 LIMIT 1")
    String selectNameByNo(@Param("no") String no);
}
