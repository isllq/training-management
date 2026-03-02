package com.training.controller;

import com.training.common.ApiResponse;
import com.training.mapper.SysUserMapper;
import com.training.model.dto.LoginRequest;
import com.training.model.dto.LoginResponse;
import com.training.model.entity.SysUser;
import com.training.security.AuthContext;
import com.training.service.AuthService;
import com.training.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final SysUserMapper userMapper;
    private final UserService userService;

    public AuthController(AuthService authService, SysUserMapper userMapper, UserService userService) {
        this.authService = authService;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Validated LoginRequest request, HttpServletRequest servletRequest) {
        String ip = servletRequest.getRemoteAddr();
        return ApiResponse.success(authService.login(request, ip));
    }

    @GetMapping("/me")
    public ApiResponse<SysUser> me() {
        Long uid = AuthContext.getUserId();
        SysUser user = userMapper.selectById(uid);
        if (user != null) {
            user.setPasswordHash(null);
        }
        return ApiResponse.success(user);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody SysUser payload) {
        Long uid = AuthContext.getUserId();
        SysUser current = userMapper.selectById(uid);
        SysUser user = new SysUser();
        user.setId(uid);
        user.setRealName(payload.getRealName());
        user.setPhone(payload.getPhone());
        user.setEmail(payload.getEmail());
        user.setStatus(1);
        user.setUserType(AuthContext.getUserType());
        user.setClassName(current == null ? null : current.getClassName());
        userService.update(user);
        return ApiResponse.success();
    }

    @GetMapping("/user-options")
    public ApiResponse<List<Map<String, Object>>> userOptions(
            @RequestParam(value = "userType", required = false) String userType,
            @RequestParam(value = "className", required = false) String className) {
        String scopedClassName = className;
        if ("STUDENT".equals(AuthContext.getUserType())) {
            SysUser me = userMapper.selectById(AuthContext.getUserId());
            if (me == null || !StringUtils.hasText(me.getClassName())) {
                return ApiResponse.success(Collections.emptyList());
            }
            scopedClassName = me == null ? null : me.getClassName();
            userType = "STUDENT";
        }
        List<Map<String, Object>> options = userMapper.listOptions(userType, scopedClassName).stream()
                .map(item -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", item.getId());
                    map.put("name", item.getRealName());
                    map.put("userType", item.getUserType());
                    map.put("className", item.getClassName());
                    return map;
                })
                .collect(Collectors.toList());
        return ApiResponse.success(options);
    }
}
