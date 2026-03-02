package com.training.controller;

import com.training.common.ApiResponse;
import com.training.model.entity.SysLoginLog;
import com.training.security.RoleGuard;
import com.training.service.SystemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/login-logs")
    public ApiResponse<List<SysLoginLog>> loginLogs(@RequestParam(value = "username", required = false) String username,
                                                    @RequestParam(value = "status", required = false) Integer status) {
        RoleGuard.requireAdmin();
        return ApiResponse.success(systemService.listLoginLogs(username, status));
    }

    @GetMapping("/runtime")
    public ApiResponse<Map<String, Object>> runtime() {
        RoleGuard.requireAdmin();
        return ApiResponse.success(systemService.runtimeStatus());
    }
}
