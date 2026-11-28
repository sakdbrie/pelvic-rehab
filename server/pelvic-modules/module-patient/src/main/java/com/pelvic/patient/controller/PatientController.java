package com.pelvic.patient.controller;

import com.pelvic.common.core.result.Result;
import com.pelvic.common.security.RequirePerm;
import com.pelvic.patient.dto.PatientDetailResponse;
import com.pelvic.patient.dto.SupplementRequest;
import com.pelvic.patient.service.PatientQueryService;
import com.pelvic.patient.service.SupplementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 患者接口（后端实现逻辑.md 第 9、10 节；前缀 /api/admin/v1/patient/**） */
@RestController
@RequestMapping("/admin/v1/patient")
public class PatientController {

    @Autowired
    private PatientQueryService patientQueryService;
    @Autowired
    private SupplementService supplementService;

    /** 患者详情（路径参数为患者编号，如 P20260001） */
    @GetMapping("/{patientId}")
    public Result<PatientDetailResponse> detail(@PathVariable String patientId) {
        return Result.ok(patientQueryService.getDetail(patientId));
    }

    /** 医生补充临床检查 */
    @PutMapping("/{patientId}/supplement")
    @RequirePerm("user:patient-supplement")
    public Result<Void> supplement(@PathVariable String patientId, @Valid @RequestBody SupplementRequest request) {
        supplementService.save(patientId, request);
        return Result.ok();
    }
}
