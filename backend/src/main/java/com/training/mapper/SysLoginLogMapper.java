package com.training.mapper;

import com.training.model.entity.SysLoginLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysLoginLogMapper {
    @Insert("INSERT INTO sys_login_log(user_id, username, login_time, ip, status, message) " +
            "VALUES(#{userId}, #{username}, NOW(), #{ip}, #{status}, #{message})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysLoginLog log);

    @Select("<script>" +
            "SELECT * FROM sys_login_log WHERE 1=1 " +
            "<if test='username != null and username != \"\"'> AND username LIKE CONCAT('%',#{username},'%') </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "ORDER BY login_time DESC LIMIT 500" +
            "</script>")
    List<SysLoginLog> list(@Param("username") String username, @Param("status") Integer status);
}
