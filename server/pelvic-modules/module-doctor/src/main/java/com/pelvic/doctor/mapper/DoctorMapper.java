package com.pelvic.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.doctor.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    /** 下一个医生编号（自增起点，0 已存在时从 1 开始） */
    @Select("SELECT COALESCE(MAX(编号), 0) + 1 FROM doctor")
    Long selectNextNo();

    @Select("SELECT 医生ID AS id, 账号ID AS accountId, 编号 AS doctorNo, 科室ID AS deptId, "
            + "姓名 AS name, 性别 AS gender, 职称 AS title, 审核状态 AS auditStatus, "
            + "审核通过时间 AS auditTime, 审核人账号ID AS auditorAccountId, 通过或拒绝原因 AS auditReason, "
            + "创建时间 AS createTime, 更新时间 AS updateTime "
            + "FROM doctor WHERE 账号ID = #{accountId} LIMIT 1")
    Doctor selectByAccountId(Long accountId);

    @Select("SELECT 医生ID AS id, 账号ID AS accountId, 编号 AS doctorNo, 科室ID AS deptId, "
            + "姓名 AS name, 性别 AS gender, 职称 AS title, 审核状态 AS auditStatus, "
            + "审核通过时间 AS auditTime, 审核人账号ID AS auditorAccountId, 通过或拒绝原因 AS auditReason, "
            + "创建时间 AS createTime, 更新时间 AS updateTime "
            + "FROM doctor WHERE 编号 = #{doctorNo} LIMIT 1")
    Doctor selectByNo(Long doctorNo);

    @Select("SELECT d.医生ID AS id, d.姓名 AS name, d.性别 AS gender, d.职称 AS title, d.科室ID AS deptId, dep.科室名称 AS deptName, d.审核状态 AS auditStatus, a.手机号密文 AS phoneCipher, d.通过或拒绝原因 AS auditReason FROM doctor d JOIN department dep ON dep.科室ID = d.科室ID JOIN account a ON a.账号ID = d.账号ID WHERE d.编号 = #{doctorNo} LIMIT 1")
    DoctorDetailRow selectDetailRow(Long doctorNo);

    /** 停用账号（写在医生模块，避免改动 module-auth 的 AccountMapper） */
    @Update("UPDATE account SET 账号状态 = 'DISABLED' WHERE 账号ID = #{accountId}")
    int disableAccount(Long accountId);
}
