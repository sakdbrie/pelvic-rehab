package com.pelvic.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 控制台短信发送器（原型占位）：验证码输出到日志，方便联调。
 * TODO: 接入真实短信服务商（阿里云/腾讯云短信），替换此实现即可。
 */
@Slf4j
@Component
public class ConsoleSmsSender implements SmsSender {

    @Override
    public void send(String phone, String code) {
        String masked = phone.length() == 11
                ? phone.substring(0, 3) + "****" + phone.substring(7)
                : "***";
        log.info("【短信验证码-原型占位】发送至 {}，验证码：{}（接入真实短信服务商后移除本日志）", masked, code);
    }
}
