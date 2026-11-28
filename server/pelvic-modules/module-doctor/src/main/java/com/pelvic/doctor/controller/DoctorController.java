package com.pelvic.doctor.controller;

import com.pelvic.common.core.result.Result;
import com.pelvic.common.security.RequirePerm;
import com.pelvic.common.security.RequireRole;
import com.pelvic.doctor.dto.DoctorCreateRequest;
import com.pelvic.doctor.dto.DoctorDetailResponse;
import com.pelvic.doctor.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 医生管理接口（后端实现逻辑.md 第 7、12、13 节；前缀 /api/admin/v1/doctor/**） */
@RestController
@RequestMapping("/admin/v1/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    /** 添加医生（仅管理员） */
    @PostMapping
    @RequireRole("ADMIN")
    public Result<DoctorDetailResponse> create(@Valid @RequestBody DoctorCreateRequest request) {
        return Result.ok(doctorService.create(request));
    }

    /** 医生详情（路径参数为医生编号，如 D001） */
    @GetMapping("/{doctorId}")
    @RequirePerm("user:detail")
    public Result<DoctorDetailResponse> detail(@PathVariable String doctorId) {
        return Result.ok(doctorService.detail(doctorId));
    }

    /** 移除医生（仅管理员） */
    @DeleteMapping("/{doctorId}")
    @RequireRole("ADMIN")
    public Result<Void> remove(@PathVariable String doctorId) {
        doctorService.remove(doctorId);
        return Result.ok();
    }
}
