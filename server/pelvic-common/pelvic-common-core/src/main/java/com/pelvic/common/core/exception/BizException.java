package com.pelvic.common.core.exception;

/** 业务异常：抛出后由全局异常处理转为 HTTP 400 + 用户友好提示（message 直接展示给用户） */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
