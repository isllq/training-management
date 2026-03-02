package com.training.mapper;

import com.training.model.entity.SysUserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {
    @Select("SELECT * FROM sys_user_role WHERE user_id=#{userId} AND is_deleted=0 ORDER BY id ASC")
    List<SysUserRole> listByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_role(user_id, role_id, created_at, is_deleted) VALUES(#{userId}, #{roleId}, NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUserRole userRole);

    @Update("UPDATE sys_user_role SET is_deleted=1 WHERE user_id=#{userId} AND is_deleted=0")
    int softDeleteByUserId(@Param("userId") Long userId);
}
