package com.pelvic.report.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/** 患者数据详情响应 */
@Data
public class RecordsResponse {

    /** 患者姓名（标题展示） */
    private String patientName;

    private List<RecordRow> records;

    /** 打卡明细：key=yyyy-MM-dd */
    private Map<String, List<CheckinItem>> checkins;

    /** 9 个量表的元信息（key/name/max） */
    private List<ScaleInfo> scales;

    /** 量表填写历史：key=量表 key，value=最近 5 次（score 为占满分百分比） */
    private Map<String, List<ScalePoint>> scaleHistory;
}
