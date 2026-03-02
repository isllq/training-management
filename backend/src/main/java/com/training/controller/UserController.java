package com.training.controller;

import com.training.common.ApiResponse;
import com.training.model.entity.SysUser;
import com.training.security.RoleGuard;
import com.training.service.UserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<SysUser>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                           @RequestParam(value = "className", required = false) String className,
                                           @RequestParam(value = "sortBy", required = false) String sortBy) {
        RoleGuard.requireAdmin();
        List<SysUser> users = userService.list(keyword, className, sortBy);
        for (SysUser user : users) {
            user.setPasswordHash(null);
        }
        return ApiResponse.success(users);
    }

    @PostMapping
    public ApiResponse<SysUser> create(@RequestBody SysUser user,
                                       @RequestParam(value = "password", required = false) String password) {
        RoleGuard.requireAdmin();
        String rawPassword = StringUtils.hasText(password) ? password : "123456";
        SysUser created = userService.create(user, rawPassword);
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody SysUser user) {
        RoleGuard.requireAdmin();
        user.setId(id);
        userService.update(user);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        RoleGuard.requireAdmin();
        userService.remove(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        RoleGuard.requireAdmin();
        String password = body.get("password");
        if (!StringUtils.hasText(password)) {
            password = "123456";
        }
        userService.resetPassword(id, password);
        return ApiResponse.success();
    }

    @GetMapping("/types")
    public ApiResponse<Map<String, String>> userTypes() {
        RoleGuard.requireAdmin();
        Map<String, String> map = new HashMap<>();
        map.put("ADMIN", "管理员");
        map.put("TEACHER", "指导教师");
        map.put("STUDENT", "学生");
        return ApiResponse.success(map);
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ApiResponse<Map<String, Object>> importUsers(@RequestPart("file") MultipartFile file) {
        RoleGuard.requireAdmin();
        int successCount = userService.importUsersFromExcel(file);
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successCount);
        return ApiResponse.success(data);
    }
}
