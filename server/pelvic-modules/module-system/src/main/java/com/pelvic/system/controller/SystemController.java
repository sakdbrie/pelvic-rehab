package com.pelvic.system.controller;

import com.pelvic.common.core.result.PageResult;
import com.pelvic.common.core.result.Result;
import com.pelvic.system.dto.UserRowResp;
import com.pelvic.system.service.UserListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 系统管理接口（后端实现逻辑.md 第 5 节；前缀 /api/admin/v1/system/**） */
@RestController
@RequestMapping("/admin/v1/system")
public class SystemController {

    @Autowired
    private UserListService userListService;

    @GetMapping("/users")
    public Result<PageResult<UserRowResp>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "30") long size) {
        return Result.ok(userListService.list(keyword, role, page, size));
    }
}
