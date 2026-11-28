package com.pelvic.common.security;

import com.pelvic.common.core.exception.BizException;
import lombok.Data;

import java.util.Set;

/**
 * 登录上下文（ThreadLocal）：由 AuthInterceptor 在每次请求解析后写入，请求结束清理。
 * 身份与角色以服务端为准：上下文仅由后端会话数据构建，前端角色判断只用于界面呈现。
 */
@Data
public class AuthContext {

    private static final ThreadLocal<AuthContext> HOLDER = new ThreadLocal<>();

    private Long accountId;
    private String accountNo;
    private String nickname;
    private String clientType;
    private Long sessionId;
    private Set<String> roleCodes = Set.of();
    private Set<String> permCodes = Set.of();

    public static AuthContext get() {
        return HOLDER.get();
    }

    public static void set(AuthContext ctx) {
        HOLDER.set(ctx);
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 取当前登录上下文，未登录抛 401 */
    public static AuthContext require() {
        AuthContext ctx = HOLDER.get();
        if (ctx == null) {
            throw new BizException(401, "未登录或登录已失效");
        }
        return ctx;
    }

    public boolean isAdmin() {
        return roleCodes != null && roleCodes.contains("ADMIN");
    }

    public boolean hasPerm(String code) {
        return permCodes != null && permCodes.contains(code);
    }

    public boolean hasRole(String code) {
        return roleCodes != null && roleCodes.contains(code);
    }
}
