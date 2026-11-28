package com.pelvic.auth.service;

/** 短信发送器（原型占位：控制台输出验证码；生产接入短信服务商） */
public interface SmsSender {

    void send(String phone, String code);
}
