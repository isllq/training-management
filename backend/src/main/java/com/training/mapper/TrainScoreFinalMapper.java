package com.training.mapper;

import com.training.model.entity.TrainScoreFinal;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainScoreFinalMapper {
    @Select("<script>" +
            "SELECT s.*, p.term_name, p.class_name, tp.project_name, u.real_name AS student_name " +
            "FROM train_score_final s " +
            "LEFT JOIN train_project_publish p ON s.publish_id = p.id AND p.is_deleted = 0 " +
            "LEFT JOIN train_project tp ON p.project_id = tp.id AND tp.is_deleted = 0 " +
            "LEFT JOIN sys_user u ON s.student_id = u.id " +
            "WHERE s.is_deleted = 0 " +
            "<if test='publishId != null'> AND s.publish_id = #{publishId} </if>" +
            "ORDER BY s.id DESC" +
            "</script>")
    List<TrainScoreFinal> list(@Param("publishId") Long publishId);

    @Insert("INSERT INTO train_score_final(publish_id, student_id, usual_score, task_score, report_score, final_score, updated_at, is_deleted) " +
            "VALUES(#{publishId}, #{studentId}, #{usualScore}, #{taskScore}, #{reportScore}, #{finalScore}, NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainScoreFinal score);

    @Update("UPDATE train_score_final SET publish_id=#{publishId}, student_id=#{studentId}, usual_score=#{usualScore}, task_score=#{taskScore}, " +
            "report_score=#{reportScore}, final_score=#{finalScore}, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int update(TrainScoreFinal score);

    @Update("UPDATE train_score_final SET is_deleted=1 WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);
}
