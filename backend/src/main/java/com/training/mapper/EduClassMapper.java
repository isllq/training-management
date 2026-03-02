package com.training.mapper;

import com.training.model.entity.EduClass;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EduClassMapper {
    @Select("<script>" +
            "SELECT ec.*, m.major_name, c.college_name " +
            "FROM edu_class ec " +
            "LEFT JOIN edu_major m ON ec.major_id = m.id AND m.is_deleted = 0 " +
            "LEFT JOIN edu_college c ON m.college_id = c.id AND c.is_deleted = 0 " +
            "WHERE ec.is_deleted=0 " +
            "<if test='majorId != null'> AND ec.major_id=#{majorId} </if>" +
            "ORDER BY ec.id ASC" +
            "</script>")
    List<EduClass> list(@Param("majorId") Long majorId);

    @Insert("INSERT INTO edu_class(major_id, class_code, class_name, grade_year, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{majorId}, #{classCode}, #{className}, #{gradeYear}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EduClass eduClass);

    @Update("UPDATE edu_class SET major_id=#{majorId}, class_name=#{className}, grade_year=#{gradeYear}, status=#{status}, updated_at=NOW() " +
            "WHERE id=#{id} AND is_deleted=0")
    int update(EduClass eduClass);

    @Update("UPDATE edu_class SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);
}
