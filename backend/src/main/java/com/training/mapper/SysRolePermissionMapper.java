package com.training.mapper;

import com.training.model.entity.SysRolePermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRolePermissionMapper {
    @Select("SELECT * FROM sys_role_permission WHERE role_id=#{roleId} AND is_deleted=0 ORDER BY id ASC")
    List<SysRolePermission> listByRoleId(@Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_role_permission(role_id, permission_id, created_at, is_deleted) VALUES(#{roleId}, #{permissionId}, NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRolePermission rolePermission);

    @Update("UPDATE sys_role_permission SET is_deleted=1 WHERE role_id=#{roleId} AND is_deleted=0")
    int softDeleteByRoleId(@Param("roleId") Long roleId);
}
