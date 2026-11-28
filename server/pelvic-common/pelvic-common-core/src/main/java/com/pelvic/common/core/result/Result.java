package com.pelvic.common.core.result;

import lombok.Data;

/** 后端统一响应（与 admin-web/src/api/types.ts 的 Result<T> 约定一致） */
@Data
public class Result<T> {

    public static final int CODE_OK = 0;

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = CODE_OK;
        r.message = "ok";
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
