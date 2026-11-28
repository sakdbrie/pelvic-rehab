package com.pelvic.common.core.result;

import lombok.Data;

import java.util.List;

/** 统一分页结构（与 admin-web/src/api/types.ts 的 PageResult<T> 约定一致） */
@Data
public class PageResult<T> {

    private long total;
    private List<T> records;

    public PageResult() {
    }

    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public static <T> PageResult<T> of(long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
