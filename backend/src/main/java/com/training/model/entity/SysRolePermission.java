package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysRolePermission {
    private Long id;
    private Long roleId;
    private Long permissionId;
    private LocalDateTime createdAt;
    private Integer isDeleted;
}
