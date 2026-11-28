package com.pelvic.auth.service;

import com.pelvic.auth.entity.Account;
import com.pelvic.auth.entity.AuthSession;
import com.pelvic.auth.mapper.AccountMapper;
import com.pelvic.auth.mapper.AuthSessionMapper;
import com.pelvic.common.security.AuthContext;
import com.pelvic.common.security.AuthService;
import com.pelvic.common.security.AuthUserLoader;
import com.pelvic.common.security.TokenUtil;
import com.pelvic.common.security.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会话解析实现（后端实现逻辑.md 第 2.5 节“登录成功”公共流程的读取侧）：
 * token 哈希 → auth_session（未撤销/未过期）→ account（NORMAL）→ 角色权限 → 登录上下文
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthSessionMapper sessionMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AuthUserLoader authUserLoader;

    @Override
    public AuthContext parse(String accessToken) {
        AuthSession session = sessionMapper.selectByAccessHash(TokenUtil.hash(accessToken));
        if (session == null || session.getRevokeTime() != null) {
            return null;
        }
        if (session.getExpireTime() == null || session.getExpireTime().isBefore(LocalDateTime.now())) {
            return null;
        }
        Account account = accountMapper.selectById(session.getAccountId());
        if (account == null || !"NORMAL".equals(account.getStatus())) {
            return null;
        }
        UserInfo userInfo = authUserLoader.load(account.getId());
        AuthContext ctx = new AuthContext();
        ctx.setAccountId(account.getId());
        ctx.setAccountNo(account.getAccountNo());
        ctx.setNickname(account.getNickname());
        ctx.setClientType(session.getClientType());
        ctx.setSessionId(session.getId());
        ctx.setRoleCodes(userInfo.getRoles().stream().map(UserInfo.RoleInfo::getCode).collect(Collectors.toSet()));
        ctx.setPermCodes(Set.copyOf(userInfo.getPerms()));
        sessionMapper.touch(session.getId());
        return ctx;
    }
}
