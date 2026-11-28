package com.pelvic.auth.controller;

import com.pelvic.auth.dto.ChangePasswordRequest;
import com.pelvic.auth.dto.LoginRequest;
import com.pelvic.auth.dto.LoginResponse;
import com.pelvic.auth.dto.MeInfoResponse;
import com.pelvic.auth.dto.SmsCodeRequest;
import com.pelvic.auth.service.AccountSecurityService;
import com.pelvic.auth.service.LoginService;
import com.pelvic.auth.service.SmsCodeService;
import com.pelvic.common.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 认证授权接口（后端实现逻辑.md 第 1、2、27、28 节；前缀 /api/admin/v1/auth/**） */
@RestController
@RequestMapping("/admin/v1/auth")
public class AuthController {

    @Autowired
    private SmsCodeService smsCodeService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private AccountSecurityService accountSecurityService;

    /** 发送验证码 */
    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request, HttpServletRequest httpRequest) {
        smsCodeService.send(request.getPhone(), request.getScene(), clientIp(httpRequest));
        return Result.ok();
    }

    /** 密码登录 */
    @PostMapping("/login/password")
    public Result<LoginResponse> loginByPassword(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.ok(loginService.loginByPassword(request.getPhone(), request.getPassword(),
                clientIp(httpRequest), httpRequest.getHeader("User-Agent")));
    }

    /** 短信验证码登录 */
    @PostMapping("/login/sms")
    public Result<LoginResponse> loginBySms(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.ok(loginService.loginBySms(request.getPhone(), request.getSmsCode(),
                clientIp(httpRequest), httpRequest.getHeader("User-Agent")));
    }

    /** 当前登录用户信息（个人主页） */
    @GetMapping("/me")
    public Result<MeInfoResponse> me() {
        return Result.ok(accountSecurityService.me());
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public Result<Void> logout() {
        accountSecurityService.logout();
        return Result.ok();
    }

    /** 修改密码 */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accountSecurityService.changePassword(request.getOldPassword(), request.getNewPassword());
        return Result.ok();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
