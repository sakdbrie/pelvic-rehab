package com.pelvic.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 量表元信息（前端图例 / 满分换算） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaleInfo {

    private String key;

    private String name;

    private int max;
}
