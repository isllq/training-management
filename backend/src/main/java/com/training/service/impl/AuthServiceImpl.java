package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.SysLoginLogMapper;
import com.training.mapper.SysUserMapper;
import com.training.model.dto.LoginRequest;
import com.training.model.dto.LoginResponse;
import com.training.model.entity.SysLoginLog;
import com.training.model.entity.SysUser;
import com.training.security.JwtUtil;
import com.training.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper userMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(SysUserMapper userMapper,
                           SysLoginLogMapper loginLogMapper,
                           JwtUtil jwtUtil,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request, String ip) {
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            saveLoginLog(null, request.getUsername(), ip, 0, "用户名不存在或已禁用");
            throw new BizException(401, "用户名不存在或已禁用");
        }
        boolean passOk;
        if (user.getPasswordHash() != null && user.getPasswordHash().startsWith("$2")) {
            passOk = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        } else {
            passOk = request.getPassword().equals(user.getPasswordHash());
        }
        if (!passOk) {
            saveLoginLog(user.getId(), user.getUsername(), ip, 0, "密码错误");
            throw new BizException(401, "密码错误");
        }
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setUserType(user.getUserType());
        response.setClassName(user.getClassName());
        response.setToken(jwtUtil.generateToken(user.getId(), user.getUserType(), user.getUsername()));
        saveLoginLog(user.getId(), user.getUsername(), ip, 1, "登录成功");
        return response;
    }

    private void saveLoginLog(Long userId, String username, String ip, Integer status, String message) {
        SysLoginLog log = new SysLoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setIp(ip);
        log.setStatus(status);
        log.setMessage(message);
        loginLogMapper.insert(log);
    }
}
