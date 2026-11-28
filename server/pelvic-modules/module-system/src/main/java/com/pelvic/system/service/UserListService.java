package com.pelvic.system.service;

import com.pelvic.common.core.result.PageResult;
import com.pelvic.common.core.util.AesEncryptor;
import com.pelvic.common.security.AuthContext;
import com.pelvic.system.dto.UserRow;
import com.pelvic.system.dto.UserRowResp;
import com.pelvic.system.mapper.SystemUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户列表（后端实现逻辑.md 第 5 节）：
 * 数据范围服务端强制——管理员全部、普通医生仅患者（不信任前端 role 参数）；手机号脱敏返回。
 */
@Slf4j
@Service
public class UserListService {

    @Autowired
    private SystemUserMapper systemUserMapper;
    @Autowired
    private AesEncryptor aesEncryptor;

    public PageResult<UserRowResp> list(String keyword, String role, long page, long size) {
        AuthContext ctx = AuthContext.require();
        String effectiveRole = ("patient".equals(role) || "doctor".equals(role)) ? role : null;
        if (!ctx.isAdmin()) {
            effectiveRole = "patient";
        }
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        long offset = (Math.max(page, 1) - 1) * size;
        long total = systemUserMapper.countUsers(effectiveRole, kw);
        List<UserRow> rows = systemUserMapper.selectUsers(effectiveRole, kw, offset, size);
        List<UserRowResp> records = rows.stream().map(this::toResp).toList();
        return PageResult.of(total, records);
    }

    private UserRowResp toResp(UserRow row) {
        UserRowResp resp = new UserRowResp();
        resp.setId(row.getId());
        resp.setName(row.getName());
        resp.setRole(row.getRole());
        resp.setPhone(mask(row.getPhoneCipher()));
        return resp;
    }

    private String mask(String cipher) {
        try {
            String phone = aesEncryptor.decrypt(cipher);
            if (phone != null && phone.length() == 11) {
                return phone.substring(0, 3) + "****" + phone.substring(7);
            }
        } catch (Exception e) {
            log.warn("手机号解密失败（请检查 pelvic.encrypt.aes-key 与存量数据密钥一致性）");
        }
        return "****";
    }
}
