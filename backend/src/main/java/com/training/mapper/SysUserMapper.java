package com.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.model.entity.SysUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0 LIMIT 1")
    SysUser selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE id = #{id} AND is_deleted = 0 LIMIT 1")
    SysUser selectById(@Param("id") Long id);

    @Insert("INSERT INTO sys_user(username, password_hash, real_name, phone, email, user_type, class_name, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{username}, #{passwordHash}, #{realName}, #{phone}, #{email}, #{userType}, #{className}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);

    @Update("UPDATE sys_user SET real_name=#{realName}, phone=#{phone}, email=#{email}, user_type=#{userType}, class_name=#{className}, status=#{status}, updated_at=NOW() " +
            "WHERE id=#{id} AND is_deleted=0")
    int update(SysUser user);

    @Update("UPDATE sys_user SET password_hash=#{passwordHash}, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    @Update("UPDATE sys_user SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM sys_user WHERE is_deleted = 0")
    Long countAll();
}
