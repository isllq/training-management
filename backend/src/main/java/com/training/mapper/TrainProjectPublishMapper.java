package com.training.mapper;

import com.training.model.entity.TrainProjectPublish;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainProjectPublishMapper {
    @Select("<script>" +
            "SELECT p.*, tp.project_name, u.real_name AS teacher_name FROM train_project_publish p " +
            "LEFT JOIN train_project tp ON p.project_id = tp.id AND tp.is_deleted = 0 " +
            "LEFT JOIN sys_user u ON p.teacher_id = u.id " +
            "WHERE p.is_deleted = 0 " +
            "<if test='className != null and className != \"\"'> " +
            "AND p.class_name LIKE CONCAT('%',#{className},'%') " +
            "</if>" +
            "<if test='teacherId != null'> " +
            "AND p.teacher_id = #{teacherId} " +
            "</if>" +
            "ORDER BY p.id DESC" +
            "</script>")
    List<TrainProjectPublish> list(@Param("className") String className, @Param("teacherId") Long teacherId);

    @Select("SELECT p.*, tp.project_name, u.real_name AS teacher_name FROM train_project_publish p " +
            "LEFT JOIN train_project tp ON p.project_id = tp.id AND tp.is_deleted = 0 " +
            "LEFT JOIN sys_user u ON p.teacher_id = u.id " +
            "WHERE p.id=#{id} AND p.is_deleted=0")
    TrainProjectPublish selectById(@Param("id") Long id);

    @Select("SELECT p.*, tp.project_name, u.real_name AS teacher_name FROM train_project_publish p " +
            "LEFT JOIN train_project tp ON p.project_id = tp.id AND tp.is_deleted = 0 " +
            "LEFT JOIN sys_user u ON p.teacher_id = u.id " +
            "WHERE p.is_deleted=0 " +
            "AND p.term_name=#{termName} " +
            "AND tp.project_name=#{projectName} " +
            "AND (p.class_name=#{className} " +
            "OR FIND_IN_SET(#{className}, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')) > 0 " +
            "OR p.class_name LIKE CONCAT('%', #{className}, '%')) " +
            "ORDER BY p.id DESC LIMIT 1")
    TrainProjectPublish selectByTermClassAndProject(@Param("termName") String termName,
                                                    @Param("className") String className,
                                                    @Param("projectName") String projectName);

    @Insert("INSERT INTO train_project_publish(project_id, term_name, class_name, teacher_id, group_count, group_size_limit, assessment_standard, process_weight, team_weight, final_weight, start_date, end_date, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{projectId}, #{termName}, #{className}, #{teacherId}, #{groupCount}, #{groupSizeLimit}, #{assessmentStandard}, #{processWeight}, #{teamWeight}, #{finalWeight}, #{startDate}, #{endDate}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainProjectPublish publish);

    @Update("UPDATE train_project_publish SET project_id=#{projectId}, term_name=#{termName}, class_name=#{className}, teacher_id=#{teacherId}, " +
            "group_count=#{groupCount}, group_size_limit=#{groupSizeLimit}, assessment_standard=#{assessmentStandard}, process_weight=#{processWeight}, team_weight=#{teamWeight}, final_weight=#{finalWeight}, start_date=#{startDate}, end_date=#{endDate}, status=#{status}, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int update(TrainProjectPublish publish);

    @Update("UPDATE train_project_publish SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);
}
