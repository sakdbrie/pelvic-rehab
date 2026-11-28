package com.pelvic.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pelvic.auth.entity.AuthSmsCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthSmsCodeMapper extends BaseMapper<AuthSmsCode> {

    @Select("SELECT 验证码ID AS id, 手机号密文 AS phoneCipher, 手机号HMAC AS phoneHmac, 使用场景 AS scene, "
            + "验证码哈希 AS codeHash, 过期时间 AS expireTime, 使用时间 AS usedTime, "
            + "验证失败次数 AS failCount, 发送IP AS sendIp, 创建时间 AS createTime "
            + "FROM auth_sms_code WHERE 手机号HMAC = #{hmac} AND 使用场景 = #{scene} "
            + "ORDER BY 创建时间 DESC, 验证码ID DESC LIMIT 1")
    AuthSmsCode selectLatest(String hmac, String scene);

    /** 原子消费：使用时间 IS NULL 才可标记（防并发重放），返回受影响行数 */
    @Update("UPDATE auth_sms_code SET 使用时间 = NOW() WHERE 验证码ID = #{id} AND 使用时间 IS NULL")
    int markUsed(Long id);

    @Update("UPDATE auth_sms_code SET 验证失败次数 = 验证失败次数 + 1 WHERE 验证码ID = #{id}")
    int incrFailCount(Long id);
}
