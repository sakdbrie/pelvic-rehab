package com.pelvic.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.auth.entity.AccountCredential;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountCredentialMapper extends BaseMapper<AccountCredential> {

    @Select("SELECT 凭证ID AS id, 账号ID AS accountId, 密码哈希 AS passwordHash, "
            + "密码更新时间 AS passwordUpdateTime, 连续失败次数 AS failCount, 锁定截止时间 AS lockedUntil, "
            + "创建时间 AS createTime, 更新时间 AS updateTime "
            + "FROM account_credential WHERE 账号ID = #{accountId} LIMIT 1")
    AccountCredential selectByAccountId(Long accountId);

    @Update("UPDATE account_credential SET 连续失败次数 = #{failCount}, 锁定截止时间 = #{lockedUntil} WHERE 账号ID = #{accountId}")
    int updateFail(Long accountId, Integer failCount, java.time.LocalDateTime lockedUntil);

    @Update("UPDATE account_credential SET 密码哈希 = #{hash}, 密码更新时间 = NOW() WHERE 账号ID = #{accountId}")
    int updatePassword(Long accountId, String hash);
}
