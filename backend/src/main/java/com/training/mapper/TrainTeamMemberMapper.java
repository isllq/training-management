package com.training.mapper;

import com.training.model.entity.TrainTeamMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainTeamMemberMapper {
    @Select("SELECT * FROM train_team_member WHERE team_id=#{teamId} AND is_deleted=0 ORDER BY id ASC")
    List<TrainTeamMember> listByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT * FROM train_team_member WHERE id=#{id} AND is_deleted=0")
    TrainTeamMember selectById(@Param("id") Long id);

    @Select("SELECT * FROM train_team_member WHERE team_id=#{teamId} AND student_id=#{studentId} AND is_deleted=0 LIMIT 1")
    TrainTeamMember selectByTeamAndStudent(@Param("teamId") Long teamId, @Param("studentId") Long studentId);

    @Select("SELECT * FROM train_team_member WHERE team_id=#{teamId} AND student_id=#{studentId} LIMIT 1")
    TrainTeamMember selectAnyByTeamAndStudent(@Param("teamId") Long teamId, @Param("studentId") Long studentId);

    @Select("SELECT m.* FROM train_team_member m " +
            "JOIN train_team t ON m.team_id=t.id AND t.is_deleted=0 " +
            "WHERE m.student_id=#{studentId} AND t.publish_id=#{publishId} AND m.is_deleted=0 LIMIT 1")
    TrainTeamMember selectByStudentAndPublish(@Param("studentId") Long studentId, @Param("publishId") Long publishId);

    @Select("SELECT COUNT(1) FROM train_team_member WHERE team_id=#{teamId} AND is_deleted=0")
    Long countByTeamId(@Param("teamId") Long teamId);

    @Insert("INSERT INTO train_team_member(team_id, student_id, role_in_team, created_at, is_deleted) " +
            "VALUES(#{teamId}, #{studentId}, #{roleInTeam}, NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainTeamMember member);

    @Update("UPDATE train_team_member SET is_deleted=0, role_in_team=#{roleInTeam}, created_at=NOW() " +
            "WHERE team_id=#{teamId} AND student_id=#{studentId} AND is_deleted=1")
    int reviveByTeamAndStudent(@Param("teamId") Long teamId, @Param("studentId") Long studentId, @Param("roleInTeam") String roleInTeam);

    @Update("UPDATE train_team_member SET is_deleted=1 WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);

    @Update("UPDATE train_team_member SET is_deleted=1 WHERE team_id=#{teamId} AND student_id=#{studentId} AND is_deleted=0")
    int softDeleteByTeamAndStudent(@Param("teamId") Long teamId, @Param("studentId") Long studentId);

    @Update("UPDATE train_team_member SET role_in_team=#{roleInTeam} WHERE team_id=#{teamId} AND student_id=#{studentId} AND is_deleted=0")
    int updateRoleByTeamAndStudent(@Param("teamId") Long teamId, @Param("studentId") Long studentId, @Param("roleInTeam") String roleInTeam);
}
