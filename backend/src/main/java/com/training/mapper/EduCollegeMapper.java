package com.training.mapper;

import com.training.model.entity.EduCollege;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EduCollegeMapper {
    @Select("SELECT * FROM edu_college WHERE is_deleted=0 ORDER BY id ASC")
    List<EduCollege> list();

    @Insert("INSERT INTO edu_college(college_code, college_name, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{collegeCode}, #{collegeName}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EduCollege college);

    @Update("UPDATE edu_college SET college_name=#{collegeName}, status=#{status}, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int update(EduCollege college);

    @Update("UPDATE edu_college SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);
}
