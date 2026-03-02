package com.training.mapper;

import com.training.model.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysUserMapper {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0 LIMIT 1")
    SysUser selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE id = #{id} AND is_deleted = 0 LIMIT 1")
    SysUser selectById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT * FROM sys_user WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (username LIKE CONCAT('%',#{keyword},'%') OR real_name LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "<if test='className != null and className != \"\"'> AND class_name = #{className} </if>" +
            "<choose>" +
            "<when test='sortBy == \"CLASS_ASC\"'> " +
            "ORDER BY CASE WHEN class_name IS NULL OR class_name = '' THEN 1 ELSE 0 END ASC, class_name ASC, id ASC " +
            "</when>" +
            "<when test='sortBy == \"NAME_INITIAL_ASC\"'> " +
            "ORDER BY UPPER(SUBSTRING(real_name, 1, 1)) ASC, real_name ASC, id ASC " +
            "</when>" +
            "<when test='sortBy == \"ID_DESC\"'> ORDER BY id DESC </when>" +
            "<otherwise> ORDER BY id ASC </otherwise>" +
            "</choose>" +
            "</script>")
    List<SysUser> list(@Param("keyword") String keyword,
                       @Param("className") String className,
                       @Param("sortBy") String sortBy);

    @Select("<script>" +
            "SELECT id, real_name, user_type, class_name FROM sys_user " +
            "WHERE is_deleted = 0 AND status = 1 " +
            "<if test='userType != null and userType != \"\"'> AND user_type = #{userType} </if>" +
            "<if test='className != null and className != \"\"'> AND class_name = #{className} </if>" +
            "ORDER BY id ASC" +
            "</script>")
    List<SysUser> listOptions(@Param("userType") String userType, @Param("className") String className);

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
