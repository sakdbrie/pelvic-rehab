package com.pelvic.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pelvic.common.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 鉴权拦截器：
 * 1. 白名单路径放行（pelvic.security.whitelist）
 * 2. 解析 Authorization: Bearer <token> → AuthService.parse → AuthContext
 * 3. 校验 @RequirePerm / @RequireRole 注解
 * 4. 请求结束清理 ThreadLocal
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        String path = request.getRequestURI();
        if (isWhitelisted(path)) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        if (token == null || token.isBlank()) {
            writeJson(response, 401, "未登录，请先登录");
            return false;
        }
        AuthContext ctx = authService.parse(token);
        if (ctx == null) {
            writeJson(response, 401, "登录已失效，请重新登录");
            return false;
        }
        if (handler instanceof HandlerMethod hm) {
            RequirePerm perm = hm.getMethodAnnotation(RequirePerm.class);
            if (perm == null) {
                perm = hm.getBeanType().getAnnotation(RequirePerm.class);
            }
            if (perm != null) {
                for (String code : perm.value()) {
                    if (!ctx.hasPerm(code)) {
                        writeJson(response, 403, "无操作权限：" + code);
                        return false;
                    }
                }
            }
            RequireRole role = hm.getMethodAnnotation(RequireRole.class);
            if (role == null) {
                role = hm.getBeanType().getAnnotation(RequireRole.class);
            }
            if (role != null) {
                boolean matched = false;
                for (String code : role.value()) {
                    if (ctx.hasRole(code)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    writeJson(response, 403, "无角色权限");
                    return false;
                }
            }
        }
        AuthContext.set(ctx);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean isWhitelisted(String path) {
        for (String w : securityProperties.getWhitelist()) {
            if (path.startsWith(w)) {
                return true;
            }
        }
        return false;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(status, message)));
    }
}
