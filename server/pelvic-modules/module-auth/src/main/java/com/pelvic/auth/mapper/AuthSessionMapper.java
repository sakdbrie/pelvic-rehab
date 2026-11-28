package com.pelvic.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.auth.entity.AuthSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthSessionMapper extends BaseMapper<AuthSession> {

    @Select("SELECT 会话ID AS id, 账号ID AS accountId, 客户端类型 AS clientType, "
            + "AccessToken哈希 AS accessTokenHash, RefreshToken哈希 AS refreshTokenHash, "
            + "过期时间 AS expireTime, 撤销时间 AS revokeTime, 最后活跃时间 AS lastActiveTime, 创建时间 AS createTime "
            + "FROM auth_session WHERE AccessToken哈希 = #{hash} LIMIT 1")
    AuthSession selectByAccessHash(String hash);

    @Update("UPDATE auth_session SET 撤销时间 = NOW() WHERE 会话ID = #{sessionId}")
    int revokeById(Long sessionId);

    @Update("UPDATE auth_session SET 撤销时间 = NOW() WHERE 账号ID = #{accountId} AND 撤销时间 IS NULL")
    int revokeByAccountId(Long accountId);

    @Update("UPDATE auth_session SET 撤销时间 = NOW() WHERE 账号ID = #{accountId} AND 会话ID <> #{sessionId} AND 撤销时间 IS NULL")
    int revokeOtherSessions(Long accountId, Long sessionId);

    @Update("UPDATE auth_session SET 最后活跃时间 = NOW() WHERE 会话ID = #{sessionId}")
    int touch(Long sessionId);
}
