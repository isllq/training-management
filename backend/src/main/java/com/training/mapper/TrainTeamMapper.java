package com.training.mapper;

import com.training.model.entity.TrainTeam;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainTeamMapper {
    @Select("<script>" +
            "SELECT t.*, p.group_size_limit, " +
            "(SELECT COUNT(1) FROM train_team_member m WHERE m.team_id = t.id AND m.is_deleted = 0) AS member_count " +
            "FROM train_team t " +
            "LEFT JOIN train_project_publish p ON t.publish_id = p.id AND p.is_deleted = 0 " +
            "WHERE t.is_deleted = 0 " +
            "<if test='publishId != null'> AND t.publish_id = #{publishId} </if>" +
            "ORDER BY t.id DESC" +
            "</script>")
    List<TrainTeam> list(@Param("publishId") Long publishId);

    @Select("<script>" +
            "SELECT t.*, p.group_size_limit, " +
            "(SELECT COUNT(1) FROM train_team_member m WHERE m.team_id = t.id AND m.is_deleted = 0) AS member_count " +
            "FROM train_team t " +
            "JOIN train_project_publish p ON t.publish_id = p.id AND p.is_deleted = 0 " +
            "WHERE t.is_deleted = 0 " +
            "AND (p.class_name = #{className} " +
            "OR FIND_IN_SET(#{className}, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')) > 0 " +
            "OR p.class_name LIKE CONCAT('%', #{className}, '%')) " +
            "<if test='publishId != null'> AND t.publish_id = #{publishId} </if>" +
            "ORDER BY t.id DESC" +
            "</script>")
    List<TrainTeam> listByClassName(@Param("publishId") Long publishId, @Param("className") String className);

    @Select("SELECT t.*, p.group_size_limit, " +
            "(SELECT COUNT(1) FROM train_team_member m WHERE m.team_id = t.id AND m.is_deleted = 0) AS member_count " +
            "FROM train_team t " +
            "LEFT JOIN train_project_publish p ON t.publish_id = p.id AND p.is_deleted = 0 " +
            "WHERE t.id=#{id} AND t.is_deleted=0")
    TrainTeam selectById(@Param("id") Long id);

    @Insert("INSERT INTO train_team(publish_id, team_name, leader_student_id, status, review_status, review_comment, reviewed_by, reviewed_at, created_at, updated_at, is_deleted) " +
            "VALUES(#{publishId}, #{teamName}, #{leaderStudentId}, #{status}, #{reviewStatus}, #{reviewComment}, #{reviewedBy}, #{reviewedAt}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainTeam team);

    @Update("UPDATE train_team SET publish_id=#{publishId}, team_name=#{teamName}, leader_student_id=#{leaderStudentId}, " +
            "status=#{status}, review_status=#{reviewStatus}, review_comment=#{reviewComment}, reviewed_by=#{reviewedBy}, reviewed_at=#{reviewedAt}, updated_at=NOW() " +
            "WHERE id=#{id} AND is_deleted=0")
    int update(TrainTeam team);

    @Update("UPDATE train_team SET review_status=#{reviewStatus}, review_comment=#{reviewComment}, reviewed_by=#{reviewedBy}, reviewed_at=NOW(), updated_at=NOW() " +
            "WHERE id=#{id} AND is_deleted=0")
    int review(@Param("id") Long id, @Param("reviewStatus") Integer reviewStatus, @Param("reviewComment") String reviewComment,
               @Param("reviewedBy") Long reviewedBy);

    @Update("UPDATE train_team SET leader_student_id=#{leaderStudentId}, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int updateLeader(@Param("id") Long id, @Param("leaderStudentId") Long leaderStudentId);

    @Update("UPDATE train_team SET leader_student_id=NULL, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int clearLeader(@Param("id") Long id);

    @Update("UPDATE train_team SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM train_team WHERE is_deleted=0")
    Long countAll();

    @Select("SELECT COUNT(1) FROM train_team WHERE is_deleted=0 AND review_status=0")
    Long countPendingReview();
}
