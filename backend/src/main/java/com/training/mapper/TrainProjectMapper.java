package com.training.mapper;

import com.training.model.entity.TrainProject;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainProjectMapper {
    @Select("<script>" +
            "SELECT * FROM train_project WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (project_name LIKE CONCAT('%',#{keyword},'%') OR project_code LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "ORDER BY id DESC" +
            "</script>")
    List<TrainProject> list(@Param("keyword") String keyword);

    @Insert("INSERT INTO train_project(project_code, project_name, description, total_hours, difficulty, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{projectCode}, #{projectName}, #{description}, #{totalHours}, #{difficulty}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainProject project);

    @Update("UPDATE train_project SET project_name=#{projectName}, description=#{description}, total_hours=#{totalHours}, " +
            "difficulty=#{difficulty}, status=#{status}, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int update(TrainProject project);

    @Update("UPDATE train_project SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM train_project WHERE is_deleted=0")
    Long countAll();
}
