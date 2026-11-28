package com.pelvic.auth.service;

import com.pelvic.auth.dto.MeInfoResponse;
import com.pelvic.auth.entity.Account;
import com.pelvic.auth.entity.AccountCredential;
import com.pelvic.auth.mapper.AccountCredentialMapper;
import com.pelvic.auth.mapper.AccountMapper;
import com.pelvic.auth.mapper.AuthSessionMapper;
import com.pelvic.common.core.exception.BizException;
import com.pelvic.common.core.util.AesEncryptor;
import com.pelvic.common.security.AuthContext;
import com.pelvic.common.security.AuthUserLoader;
import com.pelvic.common.security.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 修改密码（后端实现逻辑.md 第 27.5 节）/ 退出登录（第 28.5 节）/ 当前用户信息（第 26 节）
 */
@Slf4j
@Service
public class AccountSecurityService {

    @Autowired
    private AccountCredentialMapper credentialMapper;
    @Autowired
    private AuthSessionMapper sessionMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AuthUserLoader authUserLoader;
    @Autowired
    private AesEncryptor aesEncryptor;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 当前登录用户信息（个人主页） */
    public MeInfoResponse me() {
        AuthContext ctx = AuthContext.require();
        Account account = accountMapper.selectById(ctx.getAccountId());
        UserInfo userInfo = authUserLoader.load(ctx.getAccountId());
        MeInfoResponse resp = new MeInfoResponse();
        resp.setAccountId(account.getId());
        resp.setAccountNo(account.getAccountNo());
        resp.setNickname(account.getNickname());
        String doctorName = accountMapper.selectDoctorName(account.getId());
        resp.setName(doctorName != null ? doctorName : account.getNickname());
        Long doctorNo = accountMapper.selectDoctorNo(account.getId());
        resp.setUserId(doctorNo != null ? "D" + String.format("%03d", doctorNo) : accountMapper.selectPatientNo(account.getId()));
        UserInfo.RoleInfo primaryRole = resolvePrimaryRole(userInfo.getRoles());
        resp.setRoleCode(primaryRole != null ? primaryRole.getCode() : null);
        resp.setRoleName(primaryRole != null ? primaryRole.getName() : null);
        resp.setPhone(mask(account.getPhoneCipher()));
        resp.setDeptName(accountMapper.selectDeptName(account.getId()));
        resp.setTitle(accountMapper.selectDoctorTitle(account.getId()));
        resp.setPermissions(userInfo.getPerms());
        return resp;
    }

    private UserInfo.RoleInfo resolvePrimaryRole(List<UserInfo.RoleInfo> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream().filter(r -> "ADMIN".equals(r.getCode())).findFirst().orElse(roles.get(0));
    }

    private String mask(String cipher) {
        try {
            String phone = aesEncryptor.decrypt(cipher);
            if (phone != null && phone.length() == 11) {
                return phone.substring(0, 3) + "****" + phone.substring(7);
            }
        } catch (Exception e) {
            log.warn("手机号解密失败（请检查 pelvic.encrypt.aes-key）");
        }
        return "****";
    }

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPassword, String newPassword) {
        AuthContext ctx = AuthContext.require();
        AccountCredential credential = credentialMapper.selectByAccountId(ctx.getAccountId());
        if (credential == null) {
            throw new BizException("该账号未设置登录密码");
        }
        if (!passwordEncoder.matches(oldPassword, credential.getPasswordHash())) {
            credentialMapper.updateFail(ctx.getAccountId(),
                    (credential.getFailCount() == null ? 0 : credential.getFailCount()) + 1, null);
            throw new BizException("原密码不正确");
        }
        credentialMapper.updatePassword(ctx.getAccountId(), passwordEncoder.encode(newPassword));
        // 修改密码后撤销该账号其他会话，强制重新登录
        sessionMapper.revokeOtherSessions(ctx.getAccountId(), ctx.getSessionId());
    }

    public void logout() {
        AuthContext ctx = AuthContext.require();
        sessionMapper.revokeById(ctx.getSessionId());
    }
}
