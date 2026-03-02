package com.training.controller;

import com.training.common.ApiResponse;
import com.training.model.entity.SysPermission;
import com.training.model.entity.SysRole;
import com.training.security.RoleGuard;
import com.training.service.RolePermissionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rbac")
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;

    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping("/roles")
    public ApiResponse<List<SysRole>> listRoles() {
        RoleGuard.requireAdmin();
        return ApiResponse.success(rolePermissionService.listRoles());
    }

    @PostMapping("/roles")
    public ApiResponse<SysRole> createRole(@RequestBody SysRole role) {
        RoleGuard.requireAdmin();
        return ApiResponse.success(rolePermissionService.createRole(role));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<Void> updateRole(@PathVariable("id") Long id, @RequestBody SysRole role) {
        RoleGuard.requireAdmin();
        role.setId(id);
        rolePermissionService.updateRole(role);
        return ApiResponse.success();
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable("id") Long id) {
        RoleGuard.requireAdmin();
        rolePermissionService.deleteRole(id);
        return ApiResponse.success();
    }

    @GetMapping("/permissions")
    public ApiResponse<List<SysPermission>> listPermissions() {
        RoleGuard.requireAdmin();
        return ApiResponse.success(rolePermissionService.listPermissions());
    }

    @PostMapping("/permissions")
    public ApiResponse<SysPermission> createPermission(@RequestBody SysPermission permission) {
        RoleGuard.requireAdmin();
        return ApiResponse.success(rolePermissionService.createPermission(permission));
    }

    @PutMapping("/permissions/{id}")
    public ApiResponse<Void> updatePermission(@PathVariable("id") Long id, @RequestBody SysPermission permission) {
        RoleGuard.requireAdmin();
        permission.setId(id);
        rolePermissionService.updatePermission(permission);
        return ApiResponse.success();
    }

    @DeleteMapping("/permissions/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable("id") Long id) {
        RoleGuard.requireAdmin();
        rolePermissionService.deletePermission(id);
        return ApiResponse.success();
    }

    @GetMapping("/users/{userId}/roles")
    public ApiResponse<List<Long>> listUserRoles(@PathVariable("userId") Long userId) {
        RoleGuard.requireAdmin();
        return ApiResponse.success(rolePermissionService.listUserRoleIds(userId));
    }

    @PutMapping("/users/{userId}/roles")
    public ApiResponse<Void> bindUserRoles(@PathVariable("userId") Long userId, @RequestBody Map<String, List<Long>> body) {
        RoleGuard.requireAdmin();
        rolePermissionService.bindUserRoles(userId, body.get("roleIds"));
        return ApiResponse.success();
    }

    @GetMapping("/roles/{roleId}/permissions")
    public ApiResponse<List<Long>> listRolePermissions(@PathVariable("roleId") Long roleId) {
        RoleGuard.requireAdmin();
        return ApiResponse.success(rolePermissionService.listRolePermissionIds(roleId));
    }

    @PutMapping("/roles/{roleId}/permissions")
    public ApiResponse<Void> bindRolePermissions(@PathVariable("roleId") Long roleId, @RequestBody Map<String, List<Long>> body) {
        RoleGuard.requireAdmin();
        rolePermissionService.bindRolePermissions(roleId, body.get("permissionIds"));
        return ApiResponse.success();
    }
}
