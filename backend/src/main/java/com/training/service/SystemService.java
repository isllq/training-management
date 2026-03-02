package com.training.service;

import com.training.model.entity.SysLoginLog;

import java.util.List;
import java.util.Map;

public interface SystemService {
    List<SysLoginLog> listLoginLogs(String username, Integer status);

    Map<String, Object> runtimeStatus();
}
