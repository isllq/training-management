package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.*;
import com.training.model.entity.*;
import com.training.service.RolePermissionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    public RolePermissionServiceImpl(SysRoleMapper roleMapper, SysPermissionMapper permissionMapper,
                                     SysUserRoleMapper userRoleMapper, SysRolePermissionMapper rolePermissionMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public List<SysRole> listRoles() {
        return roleMapper.list();
    }

    @Override
    public SysRole createRole(SysRole role) {
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        roleMapper.insert(role);
        return role;
    }

    @Override
    public void updateRole(SysRole role) {
        if (roleMapper.update(role) <= 0) {
            throw new BizException("角色不存在或已删除");
        }
    }

    @Override
    public void deleteRole(Long id) {
        if (roleMapper.softDelete(id) <= 0) {
            throw new BizException("角色不存在或已删除");
        }
    }

    @Override
    public List<SysPermission> listPermissions() {
        return permissionMapper.list();
    }

    @Override
    public SysPermission createPermission(SysPermission permission) {
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    public void updatePermission(SysPermission permission) {
        if (permissionMapper.update(permission) <= 0) {
            throw new BizException("权限不存在或已删除");
        }
    }

    @Override
    public void deletePermission(Long id) {
        if (permissionMapper.softDelete(id) <= 0) {
            throw new BizException("权限不存在或已删除");
        }
    }

    @Override
    public List<Long> listUserRoleIds(Long userId) {
        List<SysUserRole> list = userRoleMapper.listByUserId(userId);
        List<Long> ids = new ArrayList<>();
        for (SysUserRole item : list) {
            ids.add(item.getRoleId());
        }
        return ids;
    }

    @Override
    public void bindUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.softDeleteByUserId(userId);
        if (roleIds == null) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public List<Long> listRolePermissionIds(Long roleId) {
        List<SysRolePermission> list = rolePermissionMapper.listByRoleId(roleId);
        List<Long> ids = new ArrayList<>();
        for (SysRolePermission item : list) {
            ids.add(item.getPermissionId());
        }
        return ids;
    }

    @Override
    public void bindRolePermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.softDeleteByRoleId(roleId);
        if (permissionIds == null) {
            return;
        }
        for (Long permissionId : permissionIds) {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePermission);
        }
    }
}
