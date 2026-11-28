package com.pelvic.report.dto;

import lombok.Data;

/** 量表趋势点（score 已换算为占满分百分比） */
@Data
public class ScalePoint {

    /** 填写日期 yyyy-MM-dd */
    private String date;

    /** 占满分百分比，保留整数 */
    private double score;

    private String name;

    private int max;
}
