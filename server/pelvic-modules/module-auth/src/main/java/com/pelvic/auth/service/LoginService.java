package com.pelvic.auth.service;

import com.pelvic.auth.dto.LoginResponse;
import com.pelvic.auth.entity.Account;
import com.pelvic.auth.entity.AccountCredential;
import com.pelvic.auth.entity.AuthSmsCode;
import com.pelvic.auth.entity.AuthSession;
import com.pelvic.auth.entity.SysLoginLog;
import com.pelvic.auth.mapper.AccountCredentialMapper;
import com.pelvic.auth.mapper.AccountMapper;
import com.pelvic.auth.mapper.AuthSmsCodeMapper;
import com.pelvic.auth.mapper.AuthSessionMapper;
import com.pelvic.auth.mapper.SysLoginLogMapper;
import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.core.util.AesEncryptor;
import com.pelvic.common.core.util.HashUtil;
import com.pelvic.common.core.util.HmacSigner;
import com.pelvic.common.security.AuthUserLoader;
import com.pelvic.common.security.SecurityProperties;
import com.pelvic.common.security.TokenUtil;
import com.pelvic.common.security.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录（后端实现逻辑.md 第 2.5 节）：
 * 密码登录：account(手机号HMAC) → 状态校验 → account_credential 哈希比对（失败次数/锁定）→ 成功
 * 短信登录：auth_sms_code 校验（过期/失败次数/哈希）→ 原子标记使用 → 不存在则建号+默认角色 → 成功
 * 成功公共流程：更新最后登录时间 → 创建 auth_session → 查角色权限 → 写 sys_login_log → 返回 token
 */
@Service
public class LoginService {

    private static final int MAX_SMS_FAIL = 5;
    private static final int PWD_LOCK_THRESHOLD = 5;
    private static final int PWD_LOCK_MINUTES = 30;

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AccountCredentialMapper credentialMapper;
    @Autowired
    private AuthSmsCodeMapper smsCodeMapper;
    @Autowired
    private AuthSessionMapper sessionMapper;
    @Autowired
    private SysLoginLogMapper loginLogMapper;
    @Autowired
    private AuthUserLoader authUserLoader;
    @Autowired
    private HmacSigner hmacSigner;
    @Autowired
    private AesEncryptor aesEncryptor;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse loginByPassword(String phone, String password, String ip, String userAgent) {
        Account account = accountMapper.selectByPhoneHmac(hmacSigner.sign(phone));
        if (account == null) {
            logLogin(null, "PASSWORD", false, "该用户不存在", ip, userAgent);
            throw new BizException("该用户不存在！");
        }
        checkStatus(account);
        AccountCredential credential = credentialMapper.selectByAccountId(account.getId());
        if (credential == null) {
            logLogin(account.getId(), "PASSWORD", false, "该账号未设置登录密码", ip, userAgent);
            throw new BizException("该账号未设置登录密码，请使用短信验证码登录");
        }
        if (credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(LocalDateTime.now())) {
            logLogin(account.getId(), "PASSWORD", false, "账号已锁定", ip, userAgent);
            throw new BizException("账号已锁定，请稍后再试");
        }
        if (password == null || !passwordEncoder.matches(password, credential.getPasswordHash())) {
            int fails = credential.getFailCount() == null ? 0 : credential.getFailCount();
            fails += 1;
            LocalDateTime lockedUntil = fails >= PWD_LOCK_THRESHOLD
                    ? LocalDateTime.now().plusMinutes(PWD_LOCK_MINUTES) : null;
            credentialMapper.updateFail(account.getId(), fails, lockedUntil);
            logLogin(account.getId(), "PASSWORD", false, "密码不正确", ip, userAgent);
            throw new BizException("密码不正确！");
        }
        credentialMapper.updateFail(account.getId(), 0, null);
        return doLoginSuccess(account, "PASSWORD", ip, userAgent);
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse loginBySms(String phone, String smsCode, String ip, String userAgent) {
        if (smsCode == null || !smsCode.matches("\\d{6}")) {
            throw new BizException("请输入 6 位短信验证码");
        }
        String hmac = hmacSigner.sign(phone);
        AuthSmsCode sms = smsCodeMapper.selectLatest(hmac, "LOGIN");
        if (sms == null || sms.getExpireTime() == null || sms.getExpireTime().isBefore(LocalDateTime.now())
                || sms.getUsedTime() != null) {
            logLogin(null, "SMS_CODE", false, "验证码已过期", ip, userAgent);
            throw new BizException("验证码已过期，请重新获取");
        }
        if (sms.getFailCount() != null && sms.getFailCount() >= MAX_SMS_FAIL) {
            logLogin(null, "SMS_CODE", false, "验证码错误次数过多", ip, userAgent);
            throw new BizException("验证码错误次数过多，请重新获取");
        }
        if (!HashUtil.sha256Hex(smsCode).equals(sms.getCodeHash())) {
            smsCodeMapper.incrFailCount(sms.getId());
            logLogin(null, "SMS_CODE", false, "验证码错误", ip, userAgent);
            throw new BizException("验证码错误！");
        }
        if (smsCodeMapper.markUsed(sms.getId()) == 0) {
            throw new BizException("验证码已被使用，请重新获取");
        }
        Account account = accountMapper.selectByPhoneHmac(hmac);
        if (account == null) {
            // 初次登录：新建账号（注册来源=WEB，手机号密文/HMAC 直接入 account）
            account = createAccount(phone, hmac);
            accountMapper.grantDefaultDoctorRole(account.getId());
        } else {
            checkStatus(account);
        }
        return doLoginSuccess(account, "SMS_CODE", ip, userAgent);
    }

    private Account createAccount(String phone, String hmac) {
        Account account = new Account();
        account.setAccountNo("ACC" + System.currentTimeMillis());
        account.setNickname("用户" + phone.substring(phone.length() - 4));
        account.setStatus("NORMAL");
        account.setRegisterSource("WEB");
        account.setPhoneCipher(aesEncryptor.encrypt(phone));
        account.setPhoneHmac(hmac);
        accountMapper.insert(account);
        return account;
    }

    private void checkStatus(Account account) {
        if (!"NORMAL".equals(account.getStatus())) {
            throw new BizException("账号不可用，请联系管理员");
        }
    }

    private LoginResponse doLoginSuccess(Account account, String loginType, String ip, String userAgent) {
        accountMapper.updateLastLoginTime(account.getId());
        String accessToken = TokenUtil.randomToken();
        String refreshToken = TokenUtil.randomToken();
        long ttl = securityProperties.getTokenTtlSeconds();
        AuthSession session = new AuthSession();
        session.setAccountId(account.getId());
        session.setClientType("WEB_ADMIN");
        session.setAccessTokenHash(TokenUtil.hash(accessToken));
        session.setRefreshTokenHash(TokenUtil.hash(refreshToken));
        session.setExpireTime(LocalDateTime.now().plusSeconds(ttl));
        sessionMapper.insert(session);

        UserInfo userInfo = authUserLoader.load(account.getId());
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(ttl);
        response.setAccount(new LoginResponse.AccountInfo(account.getId(), account.getNickname()));
        LoginResponse.UserInfo user = new LoginResponse.UserInfo();
        user.setUserId(resolveUserId(account.getId()));
        UserInfo.RoleInfo primaryRole = resolvePrimaryRole(userInfo.getRoles());
        user.setRoleCode(primaryRole != null ? primaryRole.getCode() : null);
        user.setRoleName(primaryRole != null ? primaryRole.getName() : null);
        user.setPermissions(userInfo.getPerms());
        response.setUser(user);
        logLogin(account.getId(), loginType, true, null, ip, userAgent);
        return response;
    }

    private String resolveUserId(Long accountId) {
        Long doctorNo = accountMapper.selectDoctorNo(accountId);
        if (doctorNo != null) {
            return "D" + String.format("%03d", doctorNo);
        }
        return accountMapper.selectPatientNo(accountId);
    }

    private UserInfo.RoleInfo resolvePrimaryRole(List<UserInfo.RoleInfo> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
                .filter(r -> "ADMIN".equals(r.getCode()))
                .findFirst()
                .orElse(roles.get(0));
    }

    private void logLogin(Long accountId, String loginType, boolean success, String failReason, String ip, String userAgent) {
        SysLoginLog log = new SysLoginLog();
        log.setAccountId(accountId);
        log.setLoginType(loginType);
        log.setClientType("WEB_ADMIN");
        log.setResult(success ? "SUCCESS" : "FAILED");
        log.setFailReason(failReason);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        loginLogMapper.insert(log);
    }
}
