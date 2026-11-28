package com.pelvic.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.auth.entity.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    @Select("SELECT 账号ID AS id, 手机号密文 AS phoneCipher, 手机号HMAC AS phoneHmac, 账号编号 AS accountNo, "
            + "昵称 AS nickname, 账号状态 AS status, 注册来源 AS registerSource, "
            + "最后登录时间 AS lastLoginTime, 创建时间 AS createTime, 更新时间 AS updateTime "
            + "FROM account WHERE 手机号HMAC = #{hmac} LIMIT 1")
    Account selectByPhoneHmac(String hmac);

    @Update("UPDATE account SET 最后登录时间 = NOW() WHERE 账号ID = #{accountId}")
    int updateLastLoginTime(Long accountId);

    /** 医生/患者业务身份（登录响应 user.userId 用） */
    @Select("SELECT 医生ID FROM doctor WHERE 账号ID = #{accountId} LIMIT 1")
    Long selectDoctorId(Long accountId);

    @Select("SELECT 编号 FROM doctor WHERE 账号ID = #{accountId} LIMIT 1")
    Long selectDoctorNo(Long accountId);

    @Select("SELECT 患者ID FROM patient WHERE 账号ID = #{accountId} LIMIT 1")
    Long selectPatientId(Long accountId);

    @Select("SELECT 患者编号 FROM patient WHERE 账号ID = #{accountId} LIMIT 1")
    String selectPatientNo(Long accountId);

    /** 当前用户信息（GET /auth/me） */
    @Select("SELECT d.姓名 FROM doctor d WHERE d.账号ID = #{accountId} LIMIT 1")
    String selectDoctorName(Long accountId);

    @Select("SELECT d.职称 FROM doctor d WHERE d.账号ID = #{accountId} LIMIT 1")
    String selectDoctorTitle(Long accountId);

    @Select("SELECT dep.科室名称 FROM doctor d JOIN department dep ON dep.科室ID = d.科室ID WHERE d.账号ID = #{accountId} LIMIT 1")
    String selectDeptName(Long accountId);

    /** 初次短信登录：授予默认医生角色（角色ID=2 见种子数据） */
    @Insert("INSERT INTO sys_user_role (账号ID, 角色ID, 授权时间) VALUES (#{accountId}, 2, NOW())")
    int grantDefaultDoctorRole(Long accountId);
}
