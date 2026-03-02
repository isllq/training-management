package com.training.mapper;

import com.training.model.entity.EduMajor;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EduMajorMapper {
    @Select("<script>" +
            "SELECT m.*, c.college_name " +
            "FROM edu_major m " +
            "LEFT JOIN edu_college c ON m.college_id = c.id AND c.is_deleted = 0 " +
            "WHERE m.is_deleted=0 " +
            "<if test='collegeId != null'> AND m.college_id=#{collegeId} </if>" +
            "ORDER BY m.id ASC" +
            "</script>")
    List<EduMajor> list(@Param("collegeId") Long collegeId);

    @Insert("INSERT INTO edu_major(college_id, major_code, major_name, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{collegeId}, #{majorCode}, #{majorName}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EduMajor major);

    @Update("UPDATE edu_major SET college_id=#{collegeId}, major_name=#{majorName}, status=#{status}, updated_at=NOW() " +
            "WHERE id=#{id} AND is_deleted=0")
    int update(EduMajor major);

    @Update("UPDATE edu_major SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);
}
