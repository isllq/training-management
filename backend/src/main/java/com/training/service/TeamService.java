package com.training.service;

import com.training.model.entity.TrainTeam;
import com.training.model.entity.TrainTeamMember;

import java.util.List;

public interface TeamService {
    List<TrainTeam> listTeams(Long publishId);

    List<TrainTeam> listTeamsByClass(Long publishId, String className);

    TrainTeam createTeam(TrainTeam team);

    void updateTeam(TrainTeam team);

    void deleteTeam(Long id);

    void reviewTeam(Long id, Integer reviewStatus, String reviewComment, Long reviewedBy);

    TrainTeam getTeam(Long id);

    TrainTeamMember getMember(Long memberId);

    TrainTeamMember findStudentMembership(Long studentId, Long publishId);

    List<TrainTeamMember> listMembers(Long teamId);

    TrainTeamMember addMember(TrainTeamMember member);

    void deleteMember(Long id);

    Long countMembers(Long teamId);

    TrainTeamMember findMemberByTeamAndStudent(Long teamId, Long studentId);

    void transferLeader(Long teamId, Long newLeaderStudentId);

    void clearLeader(Long teamId);
}
