package com.training.mapper;

import com.training.model.entity.TrainSubmission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainSubmissionMapper {
    @Select("<script>" +
            "SELECT * FROM train_submission WHERE is_deleted = 0 " +
            "<if test='taskId != null'> AND task_id = #{taskId} </if>" +
            "<if test='teamId != null'> AND team_id = #{teamId} </if>" +
            "ORDER BY id DESC" +
            "</script>")
    List<TrainSubmission> list(@Param("taskId") Long taskId, @Param("teamId") Long teamId);

    @Select("SELECT * FROM train_submission WHERE id=#{id} AND is_deleted=0")
    TrainSubmission selectById(@Param("id") Long id);

    @Select("SELECT * FROM train_submission WHERE task_id=#{taskId} AND student_id=#{studentId} AND is_deleted=0 ORDER BY id DESC LIMIT 1")
    TrainSubmission selectLatestByTaskAndStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    @Insert("INSERT INTO train_submission(task_id, team_id, student_id, version_no, content, file_url, submit_time, status, teacher_feedback, is_deleted) " +
            "VALUES(#{taskId}, #{teamId}, #{studentId}, #{versionNo}, #{content}, #{fileUrl}, #{submitTime}, #{status}, #{teacherFeedback}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainSubmission submission);

    @Update("UPDATE train_submission SET content=#{content}, file_url=#{fileUrl}, version_no=#{versionNo}, submit_time=#{submitTime}, status=#{status}, " +
            "teacher_feedback=#{teacherFeedback} " +
            "WHERE id=#{id} AND is_deleted=0")
    int update(TrainSubmission submission);

    @Update("UPDATE train_submission SET is_deleted=1 WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM train_submission WHERE is_deleted=0")
    Long countAll();
}
