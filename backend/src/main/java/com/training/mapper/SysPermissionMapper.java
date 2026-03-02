package com.training.mapper;

import com.training.model.entity.SysPermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysPermissionMapper {
    @Select("SELECT * FROM sys_permission WHERE is_deleted=0 ORDER BY id ASC")
    List<SysPermission> list();

    @Insert("INSERT INTO sys_permission(perm_code, perm_name, perm_type, path, method, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{permCode}, #{permName}, #{permType}, #{path}, #{method}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysPermission permission);

    @Update("UPDATE sys_permission SET perm_name=#{permName}, perm_type=#{permType}, path=#{path}, method=#{method}, status=#{status}, updated_at=NOW() " +
            "WHERE id=#{id} AND is_deleted=0")
    int update(SysPermission permission);

    @Update("UPDATE sys_permission SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);
}
