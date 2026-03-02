package com.training.mapper;

import com.training.model.entity.TrainTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainTaskMapper {
    @Select("<script>" +
            "SELECT t.*, p.term_name, p.class_name, tp.project_name, " +
            "COALESCE(NULLIF(p.group_count, 0), (SELECT COUNT(1) FROM train_team tt WHERE tt.publish_id = t.publish_id AND tt.is_deleted = 0), 0) AS expected_group_count, " +
            "(SELECT COUNT(DISTINCT s.team_id) FROM train_submission s WHERE s.task_id = t.id AND s.is_deleted = 0 AND s.team_id IS NOT NULL) AS submitted_group_count, " +
            "(SELECT GROUP_CONCAT(tt.team_name ORDER BY tt.id SEPARATOR '、') FROM train_team tt " +
            "WHERE tt.publish_id = t.publish_id AND tt.is_deleted = 0 " +
            "AND tt.id NOT IN (SELECT DISTINCT s.team_id FROM train_submission s WHERE s.task_id = t.id AND s.is_deleted = 0 AND s.team_id IS NOT NULL)) AS missing_team_names " +
            "FROM train_task t " +
            "LEFT JOIN train_project_publish p ON t.publish_id = p.id AND p.is_deleted = 0 " +
            "LEFT JOIN train_project tp ON p.project_id = tp.id AND tp.is_deleted = 0 " +
            "WHERE t.is_deleted = 0 " +
            "<if test='publishId != null'> AND t.publish_id = #{publishId} </if>" +
            "ORDER BY t.deadline ASC, t.id DESC" +
            "</script>")
    List<TrainTask> list(@Param("publishId") Long publishId);

    @Select("<script>" +
            "SELECT t.*, p.term_name, p.class_name, tp.project_name, " +
            "COALESCE(NULLIF(p.group_count, 0), (SELECT COUNT(1) FROM train_team tt WHERE tt.publish_id = t.publish_id AND tt.is_deleted = 0), 0) AS expected_group_count, " +
            "(SELECT COUNT(DISTINCT s.team_id) FROM train_submission s WHERE s.task_id = t.id AND s.is_deleted = 0 AND s.team_id IS NOT NULL) AS submitted_group_count, " +
            "(SELECT GROUP_CONCAT(tt.team_name ORDER BY tt.id SEPARATOR '、') FROM train_team tt " +
            "WHERE tt.publish_id = t.publish_id AND tt.is_deleted = 0 " +
            "AND tt.id NOT IN (SELECT DISTINCT s.team_id FROM train_submission s WHERE s.task_id = t.id AND s.is_deleted = 0 AND s.team_id IS NOT NULL)) AS missing_team_names " +
            "FROM train_task t " +
            "JOIN train_project_publish p ON t.publish_id = p.id AND p.is_deleted = 0 " +
            "LEFT JOIN train_project tp ON p.project_id = tp.id AND tp.is_deleted = 0 " +
            "WHERE t.is_deleted = 0 " +
            "AND (p.class_name = #{className} " +
            "OR FIND_IN_SET(#{className}, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')) > 0 " +
            "OR p.class_name LIKE CONCAT('%', #{className}, '%')) " +
            "<if test='publishId != null'> AND t.publish_id = #{publishId} </if>" +
            "ORDER BY t.deadline ASC, t.id DESC" +
            "</script>")
    List<TrainTask> listByClassName(@Param("publishId") Long publishId, @Param("className") String className);

    @Insert("INSERT INTO train_task(publish_id, stage_type, title, content, deadline, weight, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{publishId}, #{stageType}, #{title}, #{content}, #{deadline}, #{weight}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainTask task);

    @Select("SELECT * FROM train_task WHERE id=#{id} AND is_deleted=0")
    TrainTask selectById(@Param("id") Long id);

    @Update("UPDATE train_task SET publish_id=#{publishId}, stage_type=#{stageType}, title=#{title}, content=#{content}, deadline=#{deadline}, weight=#{weight}, " +
            "status=#{status}, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int update(TrainTask task);

    @Update("UPDATE train_task SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM train_task WHERE is_deleted=0")
    Long countAll();
}
