package com.training.service.impl;

import com.training.mapper.SysLoginLogMapper;
import com.training.model.entity.SysLoginLog;
import com.training.service.SystemService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemServiceImpl implements SystemService {
    private final SysLoginLogMapper loginLogMapper;

    public SystemServiceImpl(SysLoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public List<SysLoginLog> listLoginLogs(String username, Integer status) {
        return loginLogMapper.list(username, status);
    }

    @Override
    public Map<String, Object> runtimeStatus() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> map = new HashMap<>();
        map.put("javaVersion", System.getProperty("java.version"));
        map.put("osName", System.getProperty("os.name"));
        map.put("availableProcessors", runtime.availableProcessors());
        map.put("maxMemoryMB", runtime.maxMemory() / 1024 / 1024);
        map.put("totalMemoryMB", runtime.totalMemory() / 1024 / 1024);
        map.put("freeMemoryMB", runtime.freeMemory() / 1024 / 1024);
        map.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
        return map;
    }
}
