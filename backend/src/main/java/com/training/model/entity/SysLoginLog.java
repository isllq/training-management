package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysLoginLog {
    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime loginTime;
    private String ip;
    private Integer status;
    private String message;
}
