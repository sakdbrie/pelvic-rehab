package com.pelvic.system.mapper;

import com.pelvic.system.dto.UserRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 用户列表查询（后端实现逻辑.md 第 5 节：keyword 模糊匹配姓名/编号，role 过滤，分页） */
@Mapper
public interface SystemUserMapper {

    String UNION = "SELECT p.患者编号 AS id, p.姓名 AS name, 'patient' AS role, a.手机号密文 AS phoneCipher "
            + "FROM patient p JOIN account a ON a.账号ID = p.账号ID WHERE p.是否删除 = 0 "
            + "UNION ALL "
            + "SELECT CONCAT('D', LPAD(d.编号, 3, '0')) AS id, d.姓名 AS name, 'doctor' AS role, a.手机号密文 AS phoneCipher "
            + "FROM doctor d JOIN account a ON a.账号ID = d.账号ID";

    @Select("<script>"
            + "SELECT t.id, t.name, t.role, t.phoneCipher FROM (" + UNION + ") t "
            + "<where>"
            + "  <if test='role != null'> AND t.role = #{role} </if>"
            + "  <if test='keyword != null'> AND (t.id LIKE CONCAT('%', #{keyword}, '%') OR t.name LIKE CONCAT('%', #{keyword}, '%')) </if>"
            + "</where>"
            + " ORDER BY t.id LIMIT #{offset}, #{size}"
            + "</script>")
    List<UserRow> selectUsers(@Param("role") String role, @Param("keyword") String keyword,
                              @Param("offset") long offset, @Param("size") long size);

    @Select("<script>"
            + "SELECT COUNT(*) FROM (" + UNION + ") t "
            + "<where>"
            + "  <if test='role != null'> AND t.role = #{role} </if>"
            + "  <if test='keyword != null'> AND (t.id LIKE CONCAT('%', #{keyword}, '%') OR t.name LIKE CONCAT('%', #{keyword}, '%')) </if>"
            + "</where>"
            + "</script>")
    long countUsers(@Param("role") String role, @Param("keyword") String keyword);
}
