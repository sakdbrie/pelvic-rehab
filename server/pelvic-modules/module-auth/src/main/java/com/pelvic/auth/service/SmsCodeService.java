package com.pelvic.auth.service;

import com.pelvic.auth.entity.AuthSmsCode;
import com.pelvic.auth.mapper.AuthSmsCodeMapper;
import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.core.util.AesEncryptor;
import com.pelvic.common.core.util.HashUtil;
import com.pelvic.common.core.util.HmacSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 发送验证码（后端实现逻辑.md 第 1.5 节）：
 * 校验 → 60 秒频控（查最近一条验证码记录）→ 生成 6 位随机码 → 写入 auth_sms_code（只存哈希）→ 发送短信
 */
@Service
public class SmsCodeService {

    private static final Set<String> SCENES = Set.of("REGISTER", "LOGIN", "BIND_PHONE", "RESET_PASSWORD");
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${pelvic.sms.code-ttl-minutes:5}")
    private int codeTtlMinutes;
    @Value("${pelvic.sms.resend-interval-seconds:60}")
    private int resendIntervalSeconds;

    @Autowired
    private AuthSmsCodeMapper smsCodeMapper;
    @Autowired
    private HmacSigner hmacSigner;
    @Autowired
    private AesEncryptor aesEncryptor;
    @Autowired
    private SmsSender smsSender;

    public void send(String phone, String scene, String ip) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BizException("请输入正确的手机号格式");
        }
        if (scene == null || !SCENES.contains(scene)) {
            throw new BizException("无效的使用场景");
        }
        String hmac = hmacSigner.sign(phone);
        AuthSmsCode latest = smsCodeMapper.selectLatest(hmac, scene);
        if (latest != null && latest.getCreateTime() != null
                && latest.getCreateTime().plusSeconds(resendIntervalSeconds).isAfter(LocalDateTime.now())) {
            throw new BizException("验证码发送过于频繁，请稍后再试");
        }
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        AuthSmsCode entity = new AuthSmsCode();
        entity.setPhoneCipher(aesEncryptor.encrypt(phone));
        entity.setPhoneHmac(hmac);
        entity.setScene(scene);
        entity.setCodeHash(HashUtil.sha256Hex(code));
        entity.setExpireTime(LocalDateTime.now().plusMinutes(codeTtlMinutes));
        entity.setFailCount(0);
        entity.setSendIp(ip);
        smsCodeMapper.insert(entity);
        smsSender.send(phone, code);
    }
}
