package com.training.service;

import com.training.model.entity.SysPermission;
import com.training.model.entity.SysRole;

import java.util.List;

public interface RolePermissionService {
    List<SysRole> listRoles();

    SysRole createRole(SysRole role);

    void updateRole(SysRole role);

    void deleteRole(Long id);

    List<SysPermission> listPermissions();

    SysPermission createPermission(SysPermission permission);

    void updatePermission(SysPermission permission);

    void deletePermission(Long id);

    List<Long> listUserRoleIds(Long userId);

    void bindUserRoles(Long userId, List<Long> roleIds);

    List<Long> listRolePermissionIds(Long roleId);

    void bindRolePermissions(Long roleId, List<Long> permissionIds);
}
