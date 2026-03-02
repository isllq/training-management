package com.training.mapper;

import com.training.model.entity.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    @Select("SELECT * FROM sys_role WHERE is_deleted=0 ORDER BY id ASC")
    List<SysRole> list();

    @Insert("INSERT INTO sys_role(role_code, role_name, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{roleCode}, #{roleName}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRole role);

    @Update("UPDATE sys_role SET role_name=#{roleName}, status=#{status}, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int update(SysRole role);

    @Update("UPDATE sys_role SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);
}
